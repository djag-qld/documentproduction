package au.gov.qld.bdm.documentproduction.web;

public class SigningRequest {

	private String reason;
	private String signatory;
	private String data;
	private String alias;
	private String contactInfo;
	private String location;

	public String getData() {
		return data;
	}

	public String getSignatory() {
		return signatory;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public void setSignatory(String signatory) {
		this.signatory = signatory;
	}

	public void setData(String data) {
		this.data = data;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(String contactInfo) {
		this.contactInfo = contactInfo;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

}
