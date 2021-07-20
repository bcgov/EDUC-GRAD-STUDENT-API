package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.entity.GradCareerProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradCareerProgramRepository extends JpaRepository<GradCareerProgramEntity, String> {

    List<GradCareerProgramEntity> findAll();

	@Query("select c from GradCareerProgramEntity c where c.careerProgramCode=:cpCode")
	GradCareerProgramEntity existsByCareerProgramCode(String cpCode);

}
