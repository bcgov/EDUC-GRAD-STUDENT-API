package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.ExamSpecialCaseCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExamSpecialCaseCodeRepository extends JpaRepository<ExamSpecialCaseCodeEntity, String> {
}
