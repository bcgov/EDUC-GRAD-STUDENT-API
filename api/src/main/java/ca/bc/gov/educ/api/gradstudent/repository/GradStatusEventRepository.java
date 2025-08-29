package ca.bc.gov.educ.api.gradstudent.repository;

import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * The interface Student event repository.
 */
public interface GradStatusEventRepository extends JpaRepository<GradStatusEvent, UUID> {
  /**
   * Find by saga id optional.
   *
   * @param sagaId the saga id
   * @return the optional
   */
  Optional<GradStatusEvent> findBySagaId(UUID sagaId);

  /**
   * Find by saga id and event type optional.
   *
   * @param sagaId    the saga id
   * @param eventType the event type
   * @return the optional
   */
  Optional<GradStatusEvent> findBySagaIdAndEventType(UUID sagaId, String eventType);

  List<GradStatusEvent> findByEventStatusAndEventTypeIn(String eventStatus, List<String> eventTypes);
  /**
   * Find by event status list.
   *
   * @param eventStatus the event status
   * @return the list
   */
  List<GradStatusEvent> findByEventStatusOrderByCreateDate(String eventStatus);

  @Transactional
  @Modifying
  @Query("delete from GradStatusEvent where createDate <= :createDate")
  void deleteByCreateDateBefore(LocalDateTime createDate);

  Optional<GradStatusEvent> findByEventId(UUID eventId);

  @Query(value = "select event.* from STATUS_EVENT event where event.EVENT_STATUS = :eventStatus " +
          "AND event.CREATE_DATE < :createDate " +
          "AND event.EVENT_TYPE not in :eventTypes " +
          "ORDER BY event.CREATE_DATE asc " +
          "FETCH FIRST :limit ROWS ONLY", nativeQuery=true)
  List<GradStatusEvent> findAllByEventStatusAndCreateDateBeforeAndEventTypeNotInOrderByCreateDate(String eventStatus, LocalDateTime createDate, int limit, List<String> eventTypes);
}
