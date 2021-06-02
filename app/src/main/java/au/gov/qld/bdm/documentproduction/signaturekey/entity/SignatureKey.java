package au.gov.qld.bdm.documentproduction.signaturekey.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import au.gov.qld.bdm.documentproduction.entity.Audited;
import au.gov.qld.bdm.documentproduction.entity.Updatable;

@Entity
public class SignatureKey extends Audited implements SignatureKeyView, Updatable {

	@Id
	private String id;
	@Column(nullable = false) private String alias;
	@Column(nullable = false) private String agency;
	@Column(nullable = false, length = 10000) private String certificate;
	@Column(nullable = false) private String kmsId;
	@Column(nullable = false) private int version;
	@Column(nullable = false) private boolean latest;
	@Column private String timestampEndpoint;
	
	private SignatureKey() {
		super(null);
	}
	
	public SignatureKey(String createdBy) {
		super(createdBy);
		this.id = UUID.randomUUID().toString();
		this.version = 1;
	}

	@Override
	public String getId() {
		return id;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setKmsId(String kmsId) {
		this.kmsId = kmsId;
	}

	public String getAlias() {
		return alias;
	}

	public String getKmsId() {
		return kmsId;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}
	
	public String getAgency() {
		return agency;
	}

	public String getCertificate() {
		return certificate;
	}

	public void setCertificate(String certificate) {
		this.certificate = certificate;
	}

	@Override
	public int getVersion() {
		return version;
	}

	@Override
	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public boolean isLatest() {
		return latest;
	}

	@Override
	public void setLatest(boolean latest) {
		this.latest = latest;
	}

	public String getTimestampEndpoint() {
		return timestampEndpoint;
	}

	public void setTimestampEndpoint(String timestampEndpoint) {
		this.timestampEndpoint = timestampEndpoint;
	}
}
