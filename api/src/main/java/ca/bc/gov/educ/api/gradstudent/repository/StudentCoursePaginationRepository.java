package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.dto.CourseReport;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCoursePaginationEntity;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface StudentCoursePaginationRepository extends JpaRepository<StudentCoursePaginationEntity, UUID>, JpaSpecificationExecutor<StudentCoursePaginationEntity>, StudentCoursePaginationRepositoryCustom {

}

interface StudentCoursePaginationRepositoryCustom {
    Stream<StudentCoursePaginationEntity> streamAll(Specification<StudentCoursePaginationEntity> spec);

    /**
     * Stream course data for report generation using native SQL
     * @param whereClause Optional WHERE clause
     * @return Stream of CourseReport with only required columns
     */
    Stream<CourseReport> streamForCourseReport(String whereClause);
}
