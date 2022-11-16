package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordSearchEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GraduationStudentRecordSearchRepository extends JpaRepository<GraduationStudentRecordSearchEntity, UUID>, JpaSpecificationExecutor<GraduationStudentRecordSearchEntity> {

}
