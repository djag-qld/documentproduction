package au.gov.qld.bdm.documentproduction.document.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;

@Entity
public class DocumentCounter {
	
	@SequenceGenerator(name="DOCUMENT_COUNTER_SEQ", sequenceName="DOCUMENT_COUNTER_SEQ", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="DOCUMENT_COUNTER_SEQ")
	@Id private Long id;
	
	public long getCounter() {
		return id;
	}
}
