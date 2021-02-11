package au.gov.qld.bdm.documentproduction.document.entity;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;

public interface DocumentRepository extends DataTablesRepository<Document, String> {
}
