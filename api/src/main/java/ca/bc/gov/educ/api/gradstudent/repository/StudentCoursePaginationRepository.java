package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCoursePaginationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface StudentCoursePaginationRepository extends JpaRepository<StudentCoursePaginationEntity, UUID>, JpaSpecificationExecutor<StudentCoursePaginationEntity> {

}
