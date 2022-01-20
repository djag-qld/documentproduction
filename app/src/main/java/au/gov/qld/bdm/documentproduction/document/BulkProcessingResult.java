package au.gov.qld.bdm.documentproduction.document;

import java.util.Date;

public class BulkProcessingResult {

	private String documentId;
	private Date processedAt;

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}
	
	public String getDocumentId() {
		return documentId;
	}

	public void setProcessedAt(Date processedAt) {
		this.processedAt = processedAt;
	}
	
	public Date getProcessedAt() {
		return processedAt;
	}

}
