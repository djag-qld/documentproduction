package au.gov.qld.bdm.documentproduction.document.entity;

import java.util.Collection;
import java.util.Date;

import au.gov.qld.bdm.documentproduction.template.entity.TemplateView;

public interface DocumentView {
	String getId();
	Date getCreated();
	String getCreatedBy();
	TemplateView getTemplate();
	Collection<DocumentSignatureView> getSignatures();
	long getCounter();
}
