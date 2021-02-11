package au.gov.qld.bdm.documentproduction.document.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import au.gov.qld.bdm.documentproduction.entity.Audited;
import au.gov.qld.bdm.documentproduction.template.entity.Template;

@Entity
public class Document extends Audited {
	
	@Id
	private String id;
	@Column(nullable = false) private String agency;
	@ManyToMany(fetch = FetchType.EAGER)
    @JoinColumn private List<DocumentSignature> signatures;
	
	@SequenceGenerator(name = "DOCUMENT_COUNTER_SEQ", sequenceName = "DOCUMENT_COUNTER_SEQ", allocationSize = 100)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DOCUMENT_COUNTER_SEQ")
	@Column private long counter;
	
	@ManyToOne
	@JoinColumn
	private Template template;
	
	private Document() {
		super(null);
		this.signatures = new ArrayList<>();
	}
	
	public Document(String createdBy) {
		super(createdBy);
		this.id = UUID.randomUUID().toString();
		this.signatures = new ArrayList<>();
	}

	@Override
	public String getId() {
		return id;
	}

	public List<DocumentSignature> getSignatures() {
		return signatures;
	}

	public String getAgency() {
		return agency;
	}

	public void setAgency(String agency) {
		this.agency = agency;
	}

	public void setSignatures(List<DocumentSignature> signatures) {
		this.signatures = signatures;
	}

	public Template getTemplate() {
		return template;
	}

	public void setTemplate(Template template) {
		this.template = template;
	}

	public long getCounter() {
		return counter;
	}

	public void setCounter(long counter) {
		this.counter = counter;
	}

}
