package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentCourseHistoryRepository extends JpaRepository<StudentCourseHistoryEntity, UUID> {

    @Query("SELECT s FROM StudentCourseHistoryEntity s WHERE s.studentID = :studentID order by s.createDate")
    List<StudentCourseHistoryEntity> findByStudentID(UUID studentID);

}
