package au.gov.qld.bdm.documentproduction.api.entity;

import java.util.Date;

public interface ApiKeyView {
	Date getCreated();
	Date getLastUsed();
	boolean isEnabled();
	String getCreatedBy();
	String getApiKeyId();
	String getAgency();
}
