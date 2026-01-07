package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface StudentOptionalProgramPaginationRepository extends JpaRepository<StudentOptionalProgramPaginationEntity, UUID>, JpaSpecificationExecutor<StudentOptionalProgramPaginationEntity>, StudentOptionalProgramPaginationRepositoryCustom {
    
}

interface StudentOptionalProgramPaginationRepositoryCustom {
    Stream<StudentOptionalProgramPaginationEntity> streamAll(Specification<StudentOptionalProgramPaginationEntity> spec);
}

