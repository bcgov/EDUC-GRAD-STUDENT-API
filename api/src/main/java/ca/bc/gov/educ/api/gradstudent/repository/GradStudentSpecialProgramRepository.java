package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.entity.GradStudentSpecialProgramEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GradStudentSpecialProgramRepository extends JpaRepository<GradStudentSpecialProgramEntity, UUID> {

	List<GradStudentSpecialProgramEntity> findByStudentID(UUID studentID);
	Optional<GradStudentSpecialProgramEntity> findByStudentIDAndSpecialProgramID(UUID studentID,UUID specialProgramID);

	@Query("select c from GradStudentSpecialProgramEntity c where c.programCode=:cpCode")
	List<GradStudentSpecialProgramEntity> existsBySpecialProgramCode(String cpCode);
}
