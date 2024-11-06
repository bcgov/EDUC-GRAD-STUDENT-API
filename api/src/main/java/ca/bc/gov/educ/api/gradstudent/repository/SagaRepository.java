package ca.bc.gov.educ.api.gradstudent.repository;


import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * The interface Saga repository.
 */
@Repository
public interface SagaRepository extends JpaRepository<SagaEntity, UUID>, JpaSpecificationExecutor<SagaEntity> {
  Optional<SagaEntity> findBySagaNameAndStatusNot(String sagaName, String status);
}
