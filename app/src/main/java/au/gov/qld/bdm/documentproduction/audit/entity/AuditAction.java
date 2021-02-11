package au.gov.qld.bdm.documentproduction.audit.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import au.gov.qld.bdm.documentproduction.entity.Audited;

@Entity
public class AuditAction extends Audited implements AuditView {

	@Id
	private String id;
	@Column(nullable = false) private String event;
	@Column(nullable = false) private String target;
	@Column(nullable = false) private String targetType;
	@Column(nullable = false) private String agency;
	@Column private String targetId;
	
	@SuppressWarnings("unused")
	private AuditAction() {
		this(null);
	}

	public AuditAction(String createdBy) {
		super(createdBy);
		this.id = UUID.randomUUID().toString();
	}

	@Override
	public String getId() {
		return id;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	@Override
	public String getEvent() {
		return event;
	}

	@Override
	public String getTarget() {
		return target;
	}

	public String getTargetType() {
		return targetType;
	}

	public void setTargetType(String targetType) {
		this.targetType = targetType;
	}

	public void setTargetId(String targetId) {
		this.targetId = targetId;
	}

	public String getTargetId() {
		return targetId;
	}
}
