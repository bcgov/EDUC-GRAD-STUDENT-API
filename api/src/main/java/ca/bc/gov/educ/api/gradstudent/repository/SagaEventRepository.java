package ca.bc.gov.educ.api.gradstudent.repository;


import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEventStatesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Saga event repository.
 */
@Repository
public interface SagaEventRepository extends JpaRepository<SagaEventStatesEntity, UUID> {
  /**
   * Find by saga list.
   *
   * @param saga the saga
   * @return the list
   */
  List<SagaEventStatesEntity> findBySaga(SagaEntity saga);

  /**
   * Find by saga and saga event outcome and saga event state and saga step number optional.
   *
   * @param saga         the saga
   * @param eventOutcome the event outcome
   * @param eventState   the event state
   * @param stepNumber   the step number
   * @return the optional
   */
  Optional<SagaEventStatesEntity> findBySagaAndSagaEventOutcomeAndSagaEventStateAndSagaStepNumber(SagaEntity saga, String eventOutcome, String eventState, int stepNumber);
}
