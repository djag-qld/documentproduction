package au.gov.qld.bdm.documentproduction.audit.entity;

import java.util.Collection;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

public interface AuditActionRepository extends DataTablesRepository<AuditAction, String> {

	Collection<AuditView> findAllByAgencyOrderByCreatedDesc(String agency);

}
