package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationLeanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentOptionalProgramPaginationLeanRepository extends JpaRepository<StudentOptionalProgramPaginationLeanEntity, UUID> {
    List<StudentOptionalProgramPaginationLeanEntity> findAllByGraduationStudentRecordIDIn(List<UUID> studentIDs);
}

