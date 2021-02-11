package au.gov.qld.bdm.documentproduction.document.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import au.gov.qld.bdm.documentproduction.entity.Audited;
import au.gov.qld.bdm.documentproduction.entity.Updatable;
import au.gov.qld.bdm.documentproduction.signaturekey.entity.SignatureKey;

@Entity
public class DocumentSignature extends Audited implements DocumentSignatureView, Updatable {

	@Id
	private String id;
	@Column(nullable = false) private String alias;
	@Column(nullable = false) private String agency;
	@ManyToOne
	@JoinColumn(nullable = false)
	private SignatureKey signatureKey;
	@Column(nullable = false) private int version;
	@Column(nullable = false) private boolean latest;
	@Column private String reasonTemplate;
	@Column private String signatoryTemplate;
	@Column private String contactInfoTemplate;
	@Column private String locationTemplate;
	
	private DocumentSignature() {
		super(null);
	}
	
	public DocumentSignature(String createdBy) {
		super(createdBy);
		this.id = UUID.randomUUID().toString();
		this.version = 1;
	}

	@Override
	public String getId() {
		return id;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public SignatureKey getSignatureKey() {
		return signatureKey;
	}

	public void setSignatureKey(SignatureKey signatureKey) {
		this.signatureKey = signatureKey;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public boolean isLatest() {
		return latest;
	}

	public void setLatest(boolean latest) {
		this.latest = latest;
	}

	public String getReasonTemplate() {
		return reasonTemplate;
	}

	public void setReasonTemplate(String reasonTemplate) {
		this.reasonTemplate = reasonTemplate;
	}

	public String getSignatoryTemplate() {
		return signatoryTemplate;
	}

	public void setSignatoryTemplate(String signatoryTemplate) {
		this.signatoryTemplate = signatoryTemplate;
	}

	public String getLocationTemplate() {
		return locationTemplate;
	}
	
	public String getContactInfoTemplate() {
		return contactInfoTemplate;
	}

	public void setContactInfoTemplate(String contactInfoTemplate) {
		this.contactInfoTemplate = contactInfoTemplate;
	}

	public void setLocationTemplate(String locationTemplate) {
		this.locationTemplate = locationTemplate;
	}

}
