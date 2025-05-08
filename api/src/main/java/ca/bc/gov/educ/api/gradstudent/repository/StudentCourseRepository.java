package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourseEntity, UUID> {

    @Query("SELECT s FROM StudentCourseEntity s WHERE s.studentID = :studentID")
    List<StudentCourseEntity> findByStudentID(UUID studentID);
}
