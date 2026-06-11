package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface ReportGradStudentPaginationRepository extends JpaRepository<ReportGradStudentDataEntity, UUID>, JpaSpecificationExecutor<ReportGradStudentDataEntity>, ReportGradStudentPaginationRepositoryCustom {
}

interface ReportGradStudentPaginationRepositoryCustom {
    Stream<ReportGradStudentDataEntity> streamAll(Specification<ReportGradStudentDataEntity> spec);
}
