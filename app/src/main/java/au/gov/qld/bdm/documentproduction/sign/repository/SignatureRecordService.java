package au.gov.qld.bdm.documentproduction.sign.repository;

import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.datatables.mapping.DataTablesInput;
import org.springframework.data.jpa.datatables.mapping.DataTablesOutput;
import org.springframework.stereotype.Service;

import au.gov.qld.bdm.documentproduction.audit.AuditBuilder;
import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;

@Service
public class SignatureRecordService {
	private static final Logger LOG = LoggerFactory.getLogger(SignatureRecordService.class);
	private final SignatureRecordRepository repository;
	private final String region;
	private final AuditService auditService;

	public SignatureRecordService(@Value("${aws.kms.region}") String region, SignatureRecordRepository repository, AuditService auditService) {
		this.region = region;
		this.repository = repository;
		this.auditService = auditService;
	}
	
	public void storeSignature(byte[] signature, String algorithm, String key, String agency) {
		SignatureRecord record = new SignatureRecord(DigestUtils.sha256Hex(signature), "SHA-256", algorithm, key, region, agency);

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

	public Optional<SignatureRecord> verify(String signatureHex, AuditableCredential credential) {
		Optional<SignatureRecord> optional = repository.findByAgencyAndSignatureHex(credential.getAgency(), signatureHex);
		if (!optional.isPresent()) {
			LOG.info("Could not find signature with agency: {} and signature: {}", credential.getAgency(), signatureHex);
			return Optional.empty();
		}
		
		LOG.info("Found signature with agency: {} and signature: {}", credential.getAgency(), signatureHex);
		String targetId = optional.get().getId();
		String targetAlias = optional.get().getId();
		String targetType = "signature";
		auditService.audit(AuditBuilder.of("verify").from(credential).target(targetId, targetAlias, targetType).build());
		return optional;
	}
}
