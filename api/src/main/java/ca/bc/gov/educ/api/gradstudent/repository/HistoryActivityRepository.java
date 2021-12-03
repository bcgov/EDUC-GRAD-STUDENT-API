package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.HistoryActivityCodeEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HistoryActivityRepository extends JpaRepository<HistoryActivityCodeEntity, String> {
}
