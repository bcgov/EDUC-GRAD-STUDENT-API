package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.entity.GraduationStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GraduationStatusRepository extends JpaRepository<GraduationStatusEntity, UUID> {

    List<GraduationStatusEntity> findAll();

	List<GraduationStatusEntity> findByRecalculateGradStatus(String recalulateFlag);

	@Query("select c from GraduationStatusEntity c where c.studentStatus=:statusCode")
	List<GraduationStatusEntity> existsByStatusCode(String statusCode);
}
