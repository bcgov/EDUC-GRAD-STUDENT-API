package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordPaginationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface GradStudentPaginationRepository extends JpaRepository<GraduationStudentRecordPaginationEntity, UUID>, JpaSpecificationExecutor<GraduationStudentRecordPaginationEntity> {

}
