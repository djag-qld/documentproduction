package au.gov.qld.bdm.documentproduction.sign;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.stream.Collectors;

import org.apache.logging.log4j.util.Strings;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.TSPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;

public class Signature implements SignatureInterface {
	private static final Logger LOG = LoggerFactory.getLogger(Signature.class);
	private final CertificateResponse certificate;
	private final ContentSignerFactory contentSignerFactory;
	private final SignatureKey key;
	private final SignatureRecordService signatureRecordService;

    public Signature(SignatureKey key, CertificateResponse certificate, ContentSignerFactory contentSignerFactory, SignatureRecordService signatureRecordService) throws IOException, GeneralSecurityException {
		this.key = key;
		this.certificate = certificate;
		this.contentSignerFactory = contentSignerFactory;
		this.signatureRecordService = signatureRecordService;
    }

    @Override
    public byte[] sign(InputStream content) throws IOException {
        try {
        	LOG.info("Applying signature from key provider");
            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            ContentSigner signer = contentSignerFactory.create(key, certificate);
            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(new JcaDigestCalculatorProviderBuilder().build()).build(signer, certificate.getSignerCertificate()));
            gen.addCertificates(new JcaCertStore(certificate.getCertificates()));
            gen.addCRLs(certificate.getCrls());
            CMSProcessableInputStream msg = new CMSProcessableInputStream(content);
            CMSSignedData signedData = gen.generate(msg, false);

            if (Strings.isNotBlank(this.key.getTimestampEndpoint())) {
            	LOG.info("Applying timestamp from: {}", this.key.getTimestampEndpoint());
                TimeStampManager timeStampManager = new TimeStampManager(this.key.getTimestampEndpoint());
                signedData = timeStampManager.addSignedTimeStamp(signedData);
            }

            LOG.info("Done applying signature from key provider");
            byte[] encoded = signedData.getEncoded();
            signatureRecordService.storeSignature(encoded, signedData.getDigestAlgorithmIDs().stream().map(aid -> aid.getAlgorithm().getId()).collect(Collectors.joining(",")), 
            		key.getKmsId(), key.getAgency());
			return encoded;
        } catch (GeneralSecurityException | CMSException | OperatorCreationException | TSPException e) {
            throw new IOException(e.getMessage(), e);
        }
    }
}