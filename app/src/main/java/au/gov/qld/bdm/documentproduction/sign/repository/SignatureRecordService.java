package au.gov.qld.bdm.documentproduction.sign.repository;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
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
	
	public void storeSignature(ByteBuffer signature, String algorithm, String key, String agency) {
		SignatureRecord record = new SignatureRecord(DigestUtils.sha256Hex(signature.array()), "SHA-256", algorithm, key, region, agency);

		LOG.info("Saving signature: {}, signatureHashAlgorithm: {}, alogirthm: {}, key: {}, region: {}, agency: {}", 
				record.getSignatureHex(), record.getSignatureHexAlgorithm(), record.getSignatureAlgorithm(), record.getKeyId(), record.getKeyRegion(), record.getAgency());
		repository.save(record);
	}

	public DataTablesOutput<SignatureView> list(@Valid DataTablesInput input, String agency) {
		DataTablesOutput<SignatureRecord> all = repository.findAll(input, (root, query, cb) -> {
			return cb.equal(root.get("agency"), agency);
		});
		DataTablesOutput<SignatureView> views = new DataTablesOutput<>();
		views.setData(all.getData().stream().map(SignatureRecordService::toView).collect(Collectors.toList()));
		views.setDraw(all.getDraw());
		views.setError(all.getError());
		views.setRecordsFiltered(all.getRecordsFiltered());
		views.setRecordsTotal(all.getRecordsTotal());
		return views;
	}
	
	private static SignatureView toView(SignatureRecord record) {
		return new SignatureView() {

			@Override
			public String getId() {
				return record.getId();
			}

			@Override
			public Date getCreatedAt() {
				return record.getCreatedAt();
			}

			@Override
			public Date getLastModifiedAt() {
				return record.getLastModifiedAt();
			}

			@Override
			public String getKeyId() {
				return record.getKeyId();
			}

			@Override
			public String getSignatureHex() {
				return record.getSignatureHex();
			}

			@Override
			public String getSignatureHexAlgorithm() {
				return record.getSignatureHexAlgorithm();
			}

			@Override
			public String getSignatureAlgorithm() {
				return record.getSignatureAlgorithm();
			}

			@Override
			public String getStatus() {
				return record.getStatus();
			}
			
		};
	}
}
