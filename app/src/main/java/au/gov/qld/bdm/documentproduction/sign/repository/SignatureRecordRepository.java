package au.gov.qld.bdm.documentproduction.sign.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, String> {

}
