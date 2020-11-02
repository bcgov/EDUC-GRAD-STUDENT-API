package ca.bc.gov.educ.api.gradstudent.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;

@Repository
public interface GradStudentRepository extends CrudRepository<GradStudentEntity, String> {

	Page<GradStudentEntity> findByStudSurname(String lastName, Pageable paging);
}
