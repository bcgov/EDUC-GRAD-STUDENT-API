package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentRecordNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StudentNoteRepository extends JpaRepository<StudentRecordNoteEntity, UUID> {

	List<StudentRecordNoteEntity> findByStudentID(UUID studentId);

	void deleteByStudentID(UUID studentID);
}
