package au.gov.qld.bdm.documentproduction.document;

import java.util.List;
import java.util.Map;

import au.gov.qld.bdm.documentproduction.audit.AuditableCredential;

public class BulkProcessingRequest {
	
	private String agency;
	private String templateAlias;
	private List<String> signatureAlias;
	private Map<String, String> templateModel;

	public List<String> getSignatureAlias() {
		return signatureAlias;
	}

	public String getTemplateAlias() {
		return templateAlias;
	}

	public AuditableCredential getCredential() {
		return new AuditableCredential() {
			@Override
			public String getId() {
				return "bulkRequest";
			}
			
			@Override
			public String getAgency() {
				return agency;
			}
		};
	}

	public Map<String, String> getTemplateModel() {
		return templateModel;
	}

}
