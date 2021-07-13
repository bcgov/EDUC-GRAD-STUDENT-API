package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.entity.GradStudentCareerProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GradStudentCareerProgramRepository extends JpaRepository<GradStudentCareerProgramEntity, UUID> {

    List<GradStudentCareerProgramEntity> findAll();

	List<GradStudentCareerProgramEntity> findByPen(String pen);

	@Query("select c from GradStudentCareerProgramEntity c where c.careerProgramCode=:cpCode")
	List<GradStudentCareerProgramEntity> existsByCareerProgramCode(String cpCode);

}
