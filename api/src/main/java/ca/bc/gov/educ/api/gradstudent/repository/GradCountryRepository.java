package ca.bc.gov.educ.api.gradstudent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.gradstudent.entity.GradCountryEntity;

@Repository
public interface GradCountryRepository extends JpaRepository<GradCountryEntity, String> {
}
