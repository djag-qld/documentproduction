package au.gov.qld.bdm.documentproduction.api.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import au.gov.qld.bdm.documentproduction.entity.Audited;

@Entity
public class ApiKey extends Audited implements ApiKeyView {

	@Id
	private String id;
	@Column(nullable = false) private String agency;
	@Column(nullable = false, unique = true) private String apiKeyId;
	@Column(nullable = false) private String apiKeyHash;
	@Column boolean enabled;
	@Column Date lastUsed;

	private ApiKey() {
		super(null);
	}
	
	public ApiKey(String createdBy) {
		super(createdBy);
		this.id = UUID.randomUUID().toString();
		this.enabled = true;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getApiKeyId() {
		return apiKeyId;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public void setApiKeyId(String apiKeyId) {
		this.apiKeyId = apiKeyId;
	}

	public String getApiKeyHash() {
		return apiKeyHash;
	}

	public void setApiKeyHash(String apiKeyHash) {
		this.apiKeyHash = apiKeyHash;
	}

	@Override
	public Date getLastUsed() {
		return lastUsed;
	}
	
	public void setLastUsed(Date lastUsed) {
		this.lastUsed = lastUsed;
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
