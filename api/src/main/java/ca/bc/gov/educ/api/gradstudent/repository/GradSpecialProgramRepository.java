package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.entity.GradSpecialProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradSpecialProgramRepository extends JpaRepository<GradSpecialProgramEntity, String> {

    List<GradSpecialProgramEntity> findAll();

	@Query("select c from GradSpecialProgramEntity c where c.specialProgramCode=:cpCode")
	GradSpecialProgramEntity existsBySpecialProgramCode(String cpCode);

}
