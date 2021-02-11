package au.gov.qld.bdm.documentproduction.audit;

import au.gov.qld.bdm.documentproduction.audit.entity.AuditAction;

public class AuditBuilder {

	private String event;
	private String targetAlias;
	private AuditableCredential auditableCredential;
	private String targetType;
	private String targetId;

	public static AuditBuilder of(String event) {
		AuditBuilder builder = new AuditBuilder();
		builder.setEvent(event);
		return builder;
	}

	private void setEvent(String event) {
		this.event = event;
	}

	public AuditBuilder target(String targetId, String targetAlias, String targetType) {
		this.targetAlias = targetAlias;
		this.targetType = targetType;
		this.targetId = targetId;
		return this;
	}
	
	public AuditBuilder from(AuditableCredential auditableCredential) {
		this.auditableCredential = auditableCredential;
		return this;
	}

	public AuditAction build() {
		AuditAction action = new AuditAction(auditableCredential.getId());
		action.setEvent(event);
		action.setTarget(targetAlias);
		action.setTargetType(targetType);
		action.setAgency(auditableCredential.getAgency());
		action.setTargetId(targetId);
		return action;
	}

}
