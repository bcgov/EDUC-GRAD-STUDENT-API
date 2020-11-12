package ca.bc.gov.educ.api.gradstudent.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ca.bc.gov.educ.api.gradstudent.model.GradProvinceEntity;

@Repository
public interface GradProvinceRepository extends JpaRepository<GradProvinceEntity, String> {

    List<GradProvinceEntity> findAll();

}
