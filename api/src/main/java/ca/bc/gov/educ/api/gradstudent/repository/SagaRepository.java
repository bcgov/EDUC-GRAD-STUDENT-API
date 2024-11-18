package ca.bc.gov.educ.api.gradstudent.repository;


import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Saga repository.
 */
@Repository
public interface SagaRepository extends JpaRepository<SagaEntity, UUID>, JpaSpecificationExecutor<SagaEntity> {
  Optional<SagaEntity> findBySagaNameAndStatusNot(String sagaName, String status);

  @Transactional
  @Modifying
  @Query("delete from SagaEntity where createDate <= :createDate")
  void deleteByCreateDateBefore(LocalDateTime createDate);

  List<SagaEntity> findAllByStatusIn(List<String> statuses);
}
