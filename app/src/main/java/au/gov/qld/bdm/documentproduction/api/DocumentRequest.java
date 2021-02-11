package au.gov.qld.bdm.documentproduction.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DocumentRequest {

	private String templateAlias = "";
	private List<String> signaturesAlias = new ArrayList<>();
	private Map<String, String> templateModel = new HashMap<>();

	public String getTemplateAlias() {
		return templateAlias;
	}

	public Map<String, String> getTemplateModel() {
		return templateModel;
	}

	public void setTemplateModel(Map<String, String> templateModel) {
		this.templateModel = templateModel;
	}

	public void setTemplateAlias(String templateAlias) {
		this.templateAlias = templateAlias;
	}

	public List<String> getSignaturesAlias() {
		return signaturesAlias;
	}

	public void setSignaturesAlias(List<String> signaturesAlias) {
		this.signaturesAlias = signaturesAlias;
	}

}
