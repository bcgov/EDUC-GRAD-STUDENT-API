package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GradStudentSearchDataEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface GradStudentSearchRepository extends JpaRepository<GradStudentSearchDataEntity, UUID>, JpaSpecificationExecutor<GradStudentSearchDataEntity>, GradStudentSearchRepositoryCustom {

}

interface GradStudentSearchRepositoryCustom {
    Stream<GradStudentSearchDataEntity> streamAll(Specification<GradStudentSearchDataEntity> spec);
}
