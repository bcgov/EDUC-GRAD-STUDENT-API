package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GradStudentSearchDataEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface GradStudentSearchRepository extends JpaRepository<GradStudentSearchDataEntity, UUID>, JpaSpecificationExecutor<GradStudentSearchDataEntity> {
}
