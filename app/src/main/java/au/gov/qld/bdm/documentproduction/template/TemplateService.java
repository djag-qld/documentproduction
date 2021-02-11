package au.gov.qld.bdm.documentproduction.template;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.gov.qld.bdm.documentproduction.audit.AuditBuilder;
import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.template.entity.Template;
import au.gov.qld.bdm.documentproduction.template.entity.TemplateRepository;
import au.gov.qld.bdm.documentproduction.template.entity.TemplateView;

@Service
public class TemplateService {
	
	private final AuditService auditService;
	private final TemplateRepository repository;

	@Autowired
	public TemplateService(AuditService auditService, TemplateRepository repository) {
		this.auditService = auditService;
		this.repository = repository;
	}

	public Optional<Template> findByAliasAndAgency(String templateAlias, String agency) {
		return repository.findTopByAliasAndAgencyOrderByVersionDesc(templateAlias, agency);
	}

	public void save(AuditableCredential credential, String alias, String content) {
		Optional<Template> existing = findByAliasAndAgency(alias, credential.getAgency());
		if (!existing.isPresent()) {
			Template entity = new Template(credential.getId());
			auditService.audit(AuditBuilder.of("templatesave").from(credential).target(entity.getId(), alias, "template").build());
			entity.setAgency(credential.getAgency());
			entity.setContent(content);
			entity.setAlias(alias);
			repository.save(entity);
			return;
		} else {
			auditService.audit(AuditBuilder.of("templatesave").from(credential).target(existing.get().getId(), alias, "template").build());
			Template template = existing.get();
			template.setLatest(false);
			template.updated(credential.getId());
			repository.save(template);
		}
		
		Template entity = new Template(credential.getId());
		auditService.audit(AuditBuilder.of("templatesave").from(credential).target(entity.getId(), alias, "template").build());
		entity.setAgency(credential.getAgency());
		entity.setContent(content);
		entity.setAlias(alias);
		entity.setVersion(existing.get().getVersion() + 1);
		entity.updated(credential.getId());
		repository.save(entity);
	}

	public Collection<TemplateView> list(String agency) {
		return repository.findAllByAgencyOrderByAliasAscVersionDesc(agency);
	}

	public Optional<TemplateView> findByAliasAndVersionAndAgency(String alias, int version, String agency) {
		return repository.findByAliasAndVersionAndAgency(alias, version, agency);
	}
	
	public Optional<Template> findByAliasAndAgencyAndVersion(String templateAlias, String agency, int version) {
		return repository.findByAliasAndAgencyAndVersion(templateAlias, agency, version);
	}
}
