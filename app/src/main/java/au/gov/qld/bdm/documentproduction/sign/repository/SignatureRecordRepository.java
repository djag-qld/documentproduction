package au.gov.qld.bdm.documentproduction.sign.repository;

import org.springframework.data.jpa.datatables.repository.DataTablesRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatureRecordRepository extends DataTablesRepository<SignatureRecord, String> {

}
