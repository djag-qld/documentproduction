package au.gov.qld.bdm.documentproduction.document;

import java.util.Collection;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.gov.qld.bdm.documentproduction.audit.AuditBuilder;
import au.gov.qld.bdm.documentproduction.audit.AuditService;
import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentSignature;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentSignatureRepository;
import au.gov.qld.bdm.documentproduction.document.entity.DocumentSignatureView;
import au.gov.qld.bdm.documentproduction.signaturekey.SignatureKeyService;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;

@Service
public class DocumentSignatureService {
	
	private final DocumentSignatureRepository repository;
	private final AuditService auditService;
	private final SignatureKeyService signatureKeyService;

	@Autowired
	public DocumentSignatureService(DocumentSignatureRepository repository, AuditService auditService, SignatureKeyService signatureKeyService) {
		this.repository = repository;
		this.auditService = auditService;
		this.signatureKeyService = signatureKeyService;
	}

	public Optional<DocumentSignature> findByAliasAndAgency(String alias, String agency) {
		return repository.findTopByAliasAndAgencyOrderByVersionDesc(alias, agency);
	}

	public void save(AuditableCredential credential, String alias, String signatureKeyAlias, int signatureKeyVersion, String reasonTemplate, String signatoryTemplate, String locationTemplate, String contactInfoTemplate) {
		Optional<SignatureKey> signatureKey = signatureKeyService.findKeyForAlias(credential.getAgency(), signatureKeyAlias, signatureKeyVersion);
		if (!signatureKey.isPresent()) {
			throw new IllegalArgumentException("No signature key found by alias: " + signatureKeyAlias + " with agency: " + credential.getAgency());
		}
		
		Optional<DocumentSignature> existing = findByAliasAndAgency(alias, credential.getAgency());
		if (!existing.isPresent()) {
			saveNewDocumentSignature(credential, alias, reasonTemplate, signatoryTemplate, locationTemplate, contactInfoTemplate, signatureKey, 1);
			return;
		}
		
		existing.get().setLatest(false);
		existing.get().updated(credential.getId());
		repository.save(existing.get());
		saveNewDocumentSignature(credential, alias, reasonTemplate, signatoryTemplate, locationTemplate, contactInfoTemplate, signatureKey, existing.get().getVersion() + 1);
	}

	private void saveNewDocumentSignature(AuditableCredential credential, String alias, String reasonTemplate, String signatoryTemplate, String locationTemplate, String contactInfoTemplate, Optional<SignatureKey> signatureKey, int version) {
		DocumentSignature entity = new DocumentSignature(credential.getId());
		auditService.audit(AuditBuilder.of("signaturesave").from(credential).target(entity.getId(), alias, "signature").build());
		entity.setAgency(credential.getAgency());
		entity.setAlias(alias);
		entity.setVersion(version);
		entity.setSignatureKey(signatureKey.get());
		entity.updated(credential.getId());
		entity.setLatest(true);
		entity.setSignatoryTemplate(signatoryTemplate);
		entity.setReasonTemplate(reasonTemplate);
		entity.setLocationTemplate(locationTemplate);
		entity.setContactInfoTemplate(contactInfoTemplate);
		repository.save(entity);
	}

	public Collection<DocumentSignatureView> list(String agency, boolean hideInactive) {
		if (hideInactive) {
			return repository.findAllByAgencyAndLatestOrderByCreatedDesc(agency, true);
		}
		return repository.findAllByAgencyOrderByCreatedDesc(agency);
	}
}
