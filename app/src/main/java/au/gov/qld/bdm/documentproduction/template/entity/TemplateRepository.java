package au.gov.qld.bdm.documentproduction.template.entity;

import java.util.Collection;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TemplateRepository extends CrudRepository<Template, String> {

	Optional<Template> findTopByAliasAndAgencyOrderByVersionDesc(String templateAlias, String agency);

	Collection<TemplateView> findAllByAgencyOrderByAliasAscVersionDesc(String agency);

	Optional<TemplateView> findByAliasAndVersionAndAgency(String alias, int version, String agency);

	Optional<Template> findByAliasAndAgencyAndVersion(String alias, String agency, int version);

	Collection<TemplateView> findAllByAgencyAndLatestOrderByAliasAscVersionDesc(String agency, boolean latest);
}
