package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCareerProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudentCareerProgramRepository extends JpaRepository<StudentCareerProgramEntity, UUID> {

    List<StudentCareerProgramEntity> findAll();

	List<StudentCareerProgramEntity> findByStudentID(UUID studentId);

	Optional<StudentCareerProgramEntity> findByStudentIDAndCareerProgramCode(UUID studentId, String careerProgramCode);

	@Query("select c from StudentCareerProgramEntity c where c.careerProgramCode=:cpCode")
	List<StudentCareerProgramEntity> existsByCareerProgramCode(String cpCode);

	void deleteByStudentID(UUID studentID);

	void deleteStudentCareerProgramEntityByStudentIDAndId(UUID studentGuid, UUID id);
}
