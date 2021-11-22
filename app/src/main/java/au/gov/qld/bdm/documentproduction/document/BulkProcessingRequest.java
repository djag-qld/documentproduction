package au.gov.qld.bdm.documentproduction.document;

import java.util.List;
import java.util.Map;

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

	public Map<String, String> getTemplateModel() {
		return templateModel;
	}

	public String getAgency() {
		return agency;
	}

}
