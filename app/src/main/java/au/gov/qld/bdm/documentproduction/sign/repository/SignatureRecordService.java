package au.gov.qld.bdm.documentproduction.sign.repository;

import java.nio.ByteBuffer;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SignatureRecordService {
	private static final Logger LOG = LoggerFactory.getLogger(SignatureRecordService.class);
	private final SignatureRecordRepository repository;
	private final String region;

	public SignatureRecordService(@Value("${aws.kms.region}") String region, SignatureRecordRepository repository) {
		this.region = region;
		this.repository = repository;
	}
	
	public void storeSignature(ByteBuffer signature, String algorithm, String key) {
		SignatureRecord record = new SignatureRecord(DigestUtils.sha256Hex(signature.array()), "SHA-256", algorithm, key, region);

		LOG.info("Saving signature: {}, signatureHashAlgorithm: {}, alogirthm: {}, key: {}, region: {}", 
				record.getSignatureHex(), record.getSignatureHexAlgorithm(), record.getSignatureAlgorithm(), record.getKeyId(), record.getKeyRegion());
		repository.save(record);
	}
}
