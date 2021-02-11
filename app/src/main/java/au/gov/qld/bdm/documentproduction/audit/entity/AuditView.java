package au.gov.qld.bdm.documentproduction.audit.entity;

import java.util.Date;

public interface AuditView {
	Date getCreated();
	String getCreatedBy();
	String getTarget();
	String getTargetType();
	String getEvent();
}
