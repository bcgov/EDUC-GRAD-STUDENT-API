package ca.bc.gov.educ.api.gradstudent.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;

@Repository
public interface GradStudentRepository extends JpaRepository<GradStudentEntity, String> {

	Page<GradStudentEntity> findByStudSurname(String lastName, Pageable paging);

	Page<GradStudentEntity> findByStudGiven(String lastName, Pageable paging);
	
	@Query("SELECT si FROM GradStudentEntity si where si.pen in :penList")	
	List<GradStudentEntity> findByPenList(List<String> penList);

	Page<GradStudentEntity> findByStudSurnameContaining(String rootUpperCase, Pageable paging);
	
	Page<GradStudentEntity> findByStudGivenContaining(String rootUpperCase, Pageable paging);

	Page<GradStudentEntity> findByStudSurnameContainingAndStudGivenContaining(String rootUpperCase,
			String rootUpperCase2, Pageable paging);

	Page<GradStudentEntity> findByStudSurnameAndStudGivenContaining(String rootUpperCase, String rootUpperCase2,
			Pageable paging);

	Page<GradStudentEntity> findByStudSurnameContainingAndStudGiven(String rootUpperCase, String rootUpperCase2,
			Pageable paging);

	Page<GradStudentEntity> findByStudSurnameAndStudGiven(String rootUpperCase, String rootUpperCase2, Pageable paging);
}
