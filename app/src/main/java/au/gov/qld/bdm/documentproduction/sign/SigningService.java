package au.gov.qld.bdm.documentproduction.sign;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.security.GeneralSecurityException;
import java.security.Security;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.security.auth.x500.X500Principal;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.bouncycastle.util.io.pem.PemObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.gov.qld.bdm.documentproduction.audit.AuditBuilder;
import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.sign.validation.AddValidationInformation;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;
import au.gov.qld.bdm.documentproduction.web.SigningRequest;

@Service
public class SigningService {
	private static final Logger LOG = LoggerFactory.getLogger(SigningService.class);
	private final ContentSignerFactory contentSignerFactory;
	private final AuditService auditService;

	@Autowired
    public SigningService(ContentSignerFactory contentSignerFactory, AuditService auditService) throws GeneralSecurityException, IOException {
		this.contentSignerFactory = contentSignerFactory;
		this.auditService = auditService;
		Security.addProvider(new BouncyCastleProvider());
    }

    public void signPdf(PDDocument doc, SignatureKey signatureKey, String signatory, String reason, String location, String contactInfo, AuditableCredential apiKey) throws GeneralSecurityException, IOException {
    	if (isBlank(signatureKey.getCertificate())) {
    		throw new IllegalStateException("No certificate applied to: " + signatureKey.getId());
    	}
    	auditService.audit(AuditBuilder.of("sign").target(signatureKey.getId(), signatureKey.getAlias(), "key").from(apiKey).build());
    	
		PDSignature pdSignature = new PDSignature();
		pdSignature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
		pdSignature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
		pdSignature.setName(signatory);
		pdSignature.setReason(reason);
		if (isNotBlank(location)) {
			pdSignature.setLocation(location);
		}
		if (isNotBlank(contactInfo)) {
			pdSignature.setContactInfo(contactInfo);
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		pdSignature.setSignDate(calendar);
		
		LOG.info("Creating signature content");
		CertificateResponse certificate = new CertificateResponse(signatureKey.getCertificate());
		Signature signature = new Signature(signatureKey, certificate, contentSignerFactory);
		SignatureOptions options = new SignatureOptions();
		options.setPreferredSignatureSize(SignatureOptions.DEFAULT_SIGNATURE_SIZE * 10);
		doc.addSignature(pdSignature, signature, options);
		LOG.info("Done creating signature content");
    }
    
    public void addWithLtv(SigningRequest signRequest, OutputStream os, Optional<AuditableCredential> credential, Optional<SignatureKey> key, InputStream data)
			throws IOException, GeneralSecurityException {
    	PDDocument doc = PDDocument.load(data);
		signPdf(doc, key.get(), signRequest.getSignatory(), signRequest.getReason(), signRequest.getLocation(), signRequest.getContactInfo(), credential.get());
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		doc.saveIncremental(bos);
		doc.close();
		bos.close();
		addLtv(new ByteArrayInputStream(bos.toByteArray()), os);
	}
    
	public String generateCsr(SignatureKey signatureKey, AuditableCredential apiKey, String subjectdn) throws IOException {
		auditService.audit(AuditBuilder.of("generateCsr").target(signatureKey.getId(), signatureKey.getAlias(), "key").from(apiKey).build());
		
		PKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(new X500Principal(subjectdn), contentSignerFactory.getPublicKey(signatureKey));
		PKCS10CertificationRequest csr = p10Builder.build(contentSignerFactory.create(signatureKey));

		PemObject pemObject = new PemObject("CERTIFICATE REQUEST", csr.getEncoded());
		StringWriter csrString = new StringWriter();
		JcaPEMWriter pemWriter = new JcaPEMWriter(csrString);
		pemWriter.writeObject(pemObject);
		pemWriter.close();
		csrString.close();
		return csrString.toString();
    }

	public void addLtv(InputStream is, OutputStream os) throws IOException {
		LOG.info("Adding validation information");
		try {
			AddValidationInformation.applyLtv(is, os);
		} catch (ExecutionException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
}
