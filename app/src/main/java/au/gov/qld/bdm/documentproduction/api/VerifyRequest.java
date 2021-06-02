package au.gov.qld.bdm.documentproduction.api;

public class VerifyRequest {
	private String signatureHex;

	public String getSignatureHex() {
		return signatureHex;
	}

	public void setSignatureHex(String signatureHex) {
		this.signatureHex = signatureHex;
	}
}
