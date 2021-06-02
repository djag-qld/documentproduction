package au.gov.qld.bdm.documentproduction.sign.repository;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SignatureRecord {
	@Id
	private String id;
	
	@Column(nullable = false, length = 1024)
	private String signatureHex;
	@Column(nullable = false)
	private String signatureAlgorithm;
	@Column(nullable = false)
	private String signatureHexAlgorithm;
	@Column(nullable = false)
	private String keyId;
	@Column(nullable = false)
	private String keyRegion;
	@Column(nullable = false)
	private Date createdAt;
	@Column(nullable = false)
	private Date lastModifiedAt;
	@Column(nullable = false)
	private String status;
	
	private SignatureRecord() {
		this.id = UUID.randomUUID().toString();
	}
	
	public SignatureRecord(String signatureHex, String signatureHexAlgorithm, String signatureAlgorithm, String keyId, String keyRegion) {
		this();
		setStatus("VALID");
		setCreatedAt(new Date());
		setLastModifiedAt(getCreatedAt());
		setKeyRegion(keyRegion);
		setKeyId(keyId);
		setSignatureAlgorithm(signatureAlgorithm);
		setSignatureHex(signatureHex);
		setSignatureHexAlgorithm(signatureHexAlgorithm);
	}

	public String getSignatureHex() {
		return signatureHex;
	}

	public void setSignatureHex(String signatureHex) {
		this.signatureHex = signatureHex;
	}

	public String getSignatureAlgorithm() {
		return signatureAlgorithm;
	}

	public void setSignatureAlgorithm(String signatureAlgorithm) {
		this.signatureAlgorithm = signatureAlgorithm;
	}

	public String getKeyId() {
		return keyId;
	}

	public void setKeyId(String keyId) {
		this.keyId = keyId;
	}

	public String getKeyRegion() {
		return keyRegion;
	}

	public void setKeyRegion(String keyRegion) {
		this.keyRegion = keyRegion;
	}

	public String getSignatureHexAlgorithm() {
		return signatureHexAlgorithm;
	}

	public void setSignatureHexAlgorithm(String signatureHexAlgorithm) {
		this.signatureHexAlgorithm = signatureHexAlgorithm;
	}

	public Date getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Date getLastModifiedAt() {
		return lastModifiedAt;
	}

	public void setLastModifiedAt(Date lastModifiedAt) {
		this.lastModifiedAt = lastModifiedAt;
	}
}
