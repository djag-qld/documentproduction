package au.gov.qld.bdm.documentproduction.document.entity;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentSignatureRepository extends CrudRepository<DocumentSignature, String> {

	Optional<DocumentSignature> findTopByAliasAndAgencyOrderByVersionDesc(String alias, String agency);

	Collection<DocumentSignatureView> findAllByAgencyOrderByCreatedDesc(String agency);

}
