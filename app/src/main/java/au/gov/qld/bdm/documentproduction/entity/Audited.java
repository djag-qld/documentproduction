package au.gov.qld.bdm.documentproduction.entity;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class Audited {
	@Column(nullable = false) private Date created;
	@Column(nullable = false) private Date lastModified;
	@Column(nullable = false) private String lastModifiedBy;
	@Column(nullable = false) private String createdBy;
	
	public Audited(String createdBy) {
		updated(createdBy);
	}
	
	public Date getCreated() {
		return new Date(created.getTime());
	}
	
	public Date getLastModified() {
		return new Date(lastModified.getTime());
	}
	
	public final void updated(String actioningUser) {
		if (isBlank(this.getCreatedBy())) {
			this.created = new Date();
			this.createdBy = actioningUser;
		}

		this.lastModifiedBy = actioningUser;
		this.lastModified = new Date();
	}

	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public String getCreatedBy() {
		return createdBy;
	}

	@Override
	public final boolean equals(Object o) {
		if (!(o instanceof Audited)) {
			return false;
		}
		
		Audited other = (Audited) o;
		return o == this || other.getId().equals(getId());
	}
	
	@Override
	public final int hashCode() {
		return getId().hashCode();
	}
	
	public abstract String getId();

}
