package au.gov.qld.bdm.documentproduction.document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.zip.Deflater;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.operator.ContentSigner;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.document.entity.Document;
import au.gov.qld.bdm.documentproduction.sign.ContentSignerFactory;
import au.gov.qld.bdm.documentproduction.sign.repository.SignatureRecordService;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;
import nl.minvws.encoding.Base45;

@Service
public class SignedQRCodeService {
	private static final String SIGNED_QRCODE_VERSION = "1.0.0";
	
	private final SignatureKeyService signatureKeyService;
	private final ContentSignerFactory contentSignerFactory;
	private final SignatureRecordService signatureRecordService;
	
	@Autowired
	public SignedQRCodeService(SignatureKeyService signatureKeyService, ContentSignerFactory contentSignerFactory,
			SignatureRecordService signatureRecordService) {
		this.signatureKeyService = signatureKeyService;
		this.contentSignerFactory = contentSignerFactory;
		this.signatureRecordService = signatureRecordService;
	}

	public String create(AuditableCredential credential, Document document, String signatureKeyAlias, Map<String, String> templateModel) {
		Optional<SignatureKey> keyForAlias = signatureKeyService.findKeyForAlias(credential.getAgency(), signatureKeyAlias);
		if (!keyForAlias.isPresent()) {
			throw new IllegalArgumentException("No signature key available for that src");
		}
		
		SignedQRContent qrContent = new SignedQRContent();
		qrContent.setF(new TreeMap<>(templateModel));
		qrContent.setKId(keyForAlias.get().getAlias() + ":" + keyForAlias.get().getVersion());
		qrContent.setDId(document.getId());
		qrContent.setVer(SIGNED_QRCODE_VERSION);
		qrContent.setCDate(new LocalDate().toString("yyyy-MM-dd"));
		try {
			ContentSigner contentSigner = contentSignerFactory.create(keyForAlias.get());
			IOUtils.write(qrContent.getSignatureContent(), contentSigner.getOutputStream(), StandardCharsets.UTF_8);
			signatureRecordService.storeSignature(contentSigner.getSignature(), contentSigner.getAlgorithmIdentifier().getAlgorithm().getId(), 
					keyForAlias.get().getKmsId(), credential.getAgency());
			
			String encodedSignature = Base45.getEncoder().encodeToString(contentSigner.getSignature());
			qrContent.setSig(encodedSignature);
			return compress(qrContent.getAllContent().getBytes(StandardCharsets.UTF_8));
		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	private String compress(byte[] data) throws IOException {
		Deflater deflater = new Deflater();
		deflater.setInput(data);

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
		deflater.finish();
		byte[] buffer = new byte[1024];
		while (!deflater.finished()) {
			int count = deflater.deflate(buffer);
			outputStream.write(buffer, 0, count);
		}
		outputStream.close();
		return Base45.getEncoder().encodeToString(outputStream.toByteArray());
	}
	
}
