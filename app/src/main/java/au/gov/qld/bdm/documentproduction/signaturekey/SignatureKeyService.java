package au.gov.qld.bdm.documentproduction.signaturekey;

import java.io.IOException;
import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.gov.qld.bdm.documentproduction.audit.AuditBuilder;
import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKeyRepository;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKeyView;

@Service
public class SignatureKeyService {
	
	private final SignatureKeyRepository repository;
	private final AuditService auditService; 
	
	@Autowired
	public SignatureKeyService(SignatureKeyRepository repository, AuditService auditService) throws IOException {
		this.repository = repository;
		this.auditService = auditService;
	}

	public Optional<SignatureKey> findKeyForAlias(String agency, String alias, int version) {
		return repository.findByAgencyAndAliasAndVersion(agency, alias, version);
	}
	
	public Optional<SignatureKey> findKeyForAlias(String agency, String alias) {
		return repository.findTopByAgencyAndAliasOrderByVersionDesc(agency, alias);
	}

	public void checkHealth() {
		repository.count();
	}

	public Collection<SignatureKeyView> list(String agency, boolean hideInactive) {
		if (hideInactive) {
			return repository.findAllByAgencyAndLatestOrderByCreatedDesc(agency, true);
		}
		return repository.findAllByAgencyOrderByCreatedDesc(agency);
	}

	public void save(AuditableCredential credential, String alias, String kmsId, String certificate, String timestampEndpoint) {
		Optional<SignatureKey> existing = repository.findTopByAgencyAndAliasOrderByVersionDesc(credential.getAgency(), alias);
		if (!existing.isPresent()) {
			SignatureKey entity = new SignatureKey(credential.getId());
			auditService.audit(AuditBuilder.of("signaturekeysave").from(credential).target(entity.getId(), alias, "signaturekey").build());
			entity.setAgency(credential.getAgency());
			entity.setCertificate(certificate);
			entity.setKmsId(kmsId);
			entity.setAlias(alias);
			entity.setLatest(true);
			entity.setTimestampEndpoint(timestampEndpoint);
			repository.save(entity);
			return;
		} 
		
		auditService.audit(AuditBuilder.of("signaturekeysave").from(credential).target(existing.get().getId(), alias, "signaturekey").build());
		existing.get().setLatest(false);
		existing.get().updated(credential.getId());
		repository.save(existing.get());
		
		SignatureKey entity = new SignatureKey(credential.getId());
		auditService.audit(AuditBuilder.of("signaturekeysave").from(credential).target(entity.getId(), alias, "signaturekey").build());
		entity.setAgency(credential.getAgency());
		entity.setCertificate(certificate);
		entity.setKmsId(kmsId);
		entity.setAlias(alias);
		entity.setLatest(true);
		entity.setTimestampEndpoint(timestampEndpoint);
		entity.setVersion(existing.get().getVersion() + 1);
		entity.updated(credential.getId());
		repository.save(entity);
	}

}
