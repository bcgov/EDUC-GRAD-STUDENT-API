package ca.bc.gov.educ.api.gradstudent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.gradstudent.entity.SchoolEntity;

@Repository
public interface SchoolRepository extends JpaRepository<SchoolEntity, String> {

    List<SchoolEntity> findAll();

	SchoolEntity findByMinCode(String mincode);

}
