package au.gov.qld.bdm.documentproduction.api;

public class DocumentResponse {

	private String documentId;
	private String data;

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setData(String data) {
		this.data = data;
	}
	
	public String getData() {
		return data;
	}
}
