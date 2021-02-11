package au.gov.qld.bdm.documentproduction.api;

import java.util.Collection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import au.gov.qld.bdm.documentproduction.api.entity.ApiKey;
import au.gov.qld.bdm.documentproduction.api.entity.ApiKeyRepository;
import au.gov.qld.bdm.documentproduction.api.entity.ApiKeyView;
import au.gov.qld.bdm.documentproduction.audit.AuditBuilder;
import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;

@Service
public class ApiKeyService {
	private static final Logger LOG = LoggerFactory.getLogger(ApiKeyService.class);
	private final ApiKeyRepository repository;
	private final AuditService auditService;

	@Autowired
	public ApiKeyService(ApiKeyRepository repository, AuditService auditService) {
		this.repository = repository;
		this.auditService = auditService;
	}

	public Collection<ApiKeyView> list(String agency) {
		return repository.findAllByAgency(agency);
	}

	public void save(AuditableCredential credential, String apiKey) {
		ApiKey entity = new ApiKey(credential.getId());
		String apiKeyId = apiKey.substring(0, apiKey.indexOf("."));
		auditService.audit(AuditBuilder.of("addapikey").target(entity.getId(), apiKeyId, "api").from(credential).build());
		entity.setAgency(credential.getAgency());
		entity.setApiKeyId(apiKeyId);
		entity.setApiKeyHash(BCrypt.hashpw(apiKey, BCrypt.gensalt()));
		repository.save(entity);
	}
	
	public void toggleEnabled(AuditableCredential credential, String apiKeyId) {
		Optional<ApiKey> apiKeyView = repository.findByApiKeyIdAndAgency(apiKeyId, credential.getAgency());
		if (!apiKeyView.isPresent()) {
			LOG.warn("Attempted to toggle key not found under agency: {} and key ID: {}", credential.getAgency(), apiKeyId);
			return;
		}
		
		
		ApiKey apiKey = apiKeyView.get();
		apiKey.updated(credential.getId());
		apiKey.setEnabled(!apiKey.isEnabled());
		auditService.audit(AuditBuilder.of(apiKey.isEnabled() ? "enable" : "disable").from(credential).target(apiKey.getId(), apiKeyId, "api").build());
		repository.save(apiKey);
	}

	public Optional<ApiKeyView> validate(String apiKey) {
		if (apiKey.indexOf(".") <= 0) {
			return Optional.empty();
		}
		Optional<ApiKey> byApiKeyId = repository.findByApiKeyId(apiKey.substring(0, apiKey.indexOf(".")));
		if (!byApiKeyId.isPresent()) {
			return Optional.empty();
		}

		ApiKey entity = byApiKeyId.get();
		if (!entity.isEnabled() || !BCrypt.checkpw(apiKey, entity.getApiKeyHash())) {
			return Optional.empty();
		}
		
		entity.updated(entity.getApiKeyId());
		entity.setLastUsed(entity.getLastModified());
		return repository.findViewByApiKeyIdAndAgency(entity.getApiKeyId(), entity.getAgency());
	}

}
