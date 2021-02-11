package au.gov.qld.bdm.documentproduction.api.entity;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

public interface ApiKeyRepository extends CrudRepository<ApiKey, String> {
	Collection<ApiKeyView> findAllByAgency(String agency);

	Optional<ApiKey> findByApiKeyId(String apiKeyId);

	Optional<ApiKey> findByApiKeyIdAndAgency(String apiKeyId, String agency);

	Optional<ApiKeyView> findViewByApiKeyIdAndAgency(String apiKeyId, String agency);
}
