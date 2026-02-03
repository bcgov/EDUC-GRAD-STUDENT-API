package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.dto.OptionalProgramReport;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface StudentOptionalProgramPaginationRepository extends JpaRepository<StudentOptionalProgramPaginationEntity, UUID>, JpaSpecificationExecutor<StudentOptionalProgramPaginationEntity>, StudentOptionalProgramPaginationRepositoryCustom {
 
    List<StudentOptionalProgramPaginationEntity> findAllByGraduationStudentRecordEntity_StudentIDIn(List<UUID> studentIDs);
}

interface StudentOptionalProgramPaginationRepositoryCustom {
    Stream<StudentOptionalProgramPaginationEntity> streamAll(Specification<StudentOptionalProgramPaginationEntity> spec);
    Stream<OptionalProgramReport> streamForOptionalProgramReport(String whereClause);
}

