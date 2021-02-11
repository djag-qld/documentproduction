package au.gov.qld.bdm.documentproduction.template.entity;

import java.util.Date;

public interface TemplateView {
	String getAlias();
	String getCreatedBy();
	Date getCreated();
	String getContent();
}
