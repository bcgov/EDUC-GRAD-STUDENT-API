package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentNonGradReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentNonGradReasonRepository extends JpaRepository<StudentNonGradReasonEntity, UUID> {
    List<StudentNonGradReasonEntity> findByPen(String pen);
}