package au.gov.qld.bdm.documentproduction.template.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import au.gov.qld.bdm.documentproduction.entity.Audited;
import au.gov.qld.bdm.documentproduction.entity.Updatable;

@Entity
public class Template extends Audited implements TemplateView, Updatable {

	@Id
	private String id;
	@Column(nullable = false) private String agency;
	@Column(nullable = false) private String alias;
	@Column(nullable = false, length = 100000) private String content;
	@Column(nullable = false) private int version;
	@Column(nullable = false) private boolean latest;
	
	private Template() {
		super(null);
	}
	
	public Template(String createdBy) {
		super(createdBy);
		this.id = UUID.randomUUID().toString();
		this.version = 1;
	}
	
	@Override
	public String getId() {
		return id;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	@Override
	public boolean isLatest() {
		return latest;
	}

	@Override
	public void setLatest(boolean latest) {
		this.latest = latest;
	}

}
