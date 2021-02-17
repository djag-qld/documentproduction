package au.gov.qld.bdm.documentproduction.signaturekey.entity;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatureKeyRepository extends CrudRepository<SignatureKey, String> {

	Optional<SignatureKey> findByAlias(String alias);

	Collection<SignatureKeyView> findAllByAgencyOrderByCreatedDesc(String agency);

	Optional<SignatureKey> findTopByAgencyAndAliasOrderByVersionDesc(String agency, String alias);

	Optional<SignatureKey> findByAgencyAndAliasAndVersion(String agency, String alias, int version);

	Collection<SignatureKeyView> findAllByAgencyAndLatestOrderByCreatedDesc(String agency, boolean latest);

}
