package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.HistoricStudentActivityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HistoricStudentActivityRepository extends JpaRepository<HistoricStudentActivityEntity, UUID> {

    List<HistoricStudentActivityEntity> findByGraduationStudentRecordID(UUID graduationStudentRecordID);
}
