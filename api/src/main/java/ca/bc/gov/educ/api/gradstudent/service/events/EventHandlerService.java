package ca.bc.gov.educ.api.gradstudent.service.events;

import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.constant.SagaEnum;
import ca.bc.gov.educ.api.gradstudent.constant.SagaStatusEnum;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.ArchiveStudentsSagaData;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.orchestrator.ArchiveStudentsOrchestrator;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.service.SagaService;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.gradstudent.constant.EventOutcome.STUDENTS_ARCHIVED;
import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.API_NAME;
import static org.springframework.transaction.annotation.Propagation.REQUIRES_NEW;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
public class EventHandlerService {

  public static final String NO_RECORD_SAGA_ID_EVENT_TYPE = "no record found for the saga id and event type combination, processing.";
  public static final String RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE = "record found for the saga id and event type combination, might be a duplicate or replay," +
          " just updating the db status so that it will be polled and sent back again.";
  public static final String EVENT_PAYLOAD = "event is :: {}";
  private final SagaService sagaService;
  private final GradStatusEventRepository gradStatusEventRepository;
  private final ArchiveStudentsOrchestrator archiveStudentsOrchestrator;
  private final GraduationStatusService graduationStatusService;

  @Autowired
  public EventHandlerService(final SagaService sagaService, ArchiveStudentsOrchestrator archiveStudentsOrchestrator, GradStatusEventRepository gradStatusEventRepository, GraduationStatusService graduationStatusService) {
    this.sagaService = sagaService;
    this.archiveStudentsOrchestrator = archiveStudentsOrchestrator;
    this.gradStatusEventRepository = gradStatusEventRepository;
    this.graduationStatusService = graduationStatusService;
  }

  @Transactional(propagation = REQUIRES_NEW)
  public byte [] handleArchiveStudentsRequest(final Event event) throws JsonProcessingException {
    final ArchiveStudentsSagaData sagaData = JsonUtil.getJsonObjectFromString(ArchiveStudentsSagaData.class, event.getEventPayload());
    final var sagaInProgress = this.sagaService.findBySagaNameAndStatusNot(SagaEnum.ARCHIVE_STUDENTS_SAGA.toString(), SagaStatusEnum.COMPLETED.toString());

    final Event newEvent = Event.builder()
      .sagaId(event.getSagaId())
      .eventType(event.getEventType()).build();

    final GradStatusEvent gradStatusEvent;
    if (sagaInProgress.isPresent()) {
      log.trace("Archive saga is already in progress. Returning conflict for this event :: {}", event);
      newEvent.setEventOutcome(EventOutcome.FAILED_TO_START_ARCHIVE_STUDENTS_SAGA);
      newEvent.setEventPayload("CONFLICT");
      gradStatusEvent = createGradStatusEventRecord(newEvent);
    } else {
      Integer numStudentsToBeArchived = this.graduationStatusService.countStudentsInSchoolOfRecordsToBeArchived(sagaData.getSchoolsOfRecords(), sagaData.getStudentStatusCode());
      newEvent.setEventOutcome(EventOutcome.ARCHIVE_STUDENTS_STARTED);
      newEvent.setEventPayload(String.valueOf(numStudentsToBeArchived));
      gradStatusEvent = createGradStatusEventRecord(newEvent);

      var saga = this.archiveStudentsOrchestrator.createSaga(event.getEventPayload(), API_NAME, sagaData.getBatchId());
      log.debug("Starting updateStudentDownstreamOrchestrator orchestrator :: {}", saga);
      this.archiveStudentsOrchestrator.startSaga(saga);
    }
    return createResponseEvent(this.gradStatusEventRepository.save(gradStatusEvent));
  }

  @Transactional(propagation = REQUIRES_NEW)
  public byte[] archiveStudents(final Event event) throws JsonProcessingException {
    var gradStatusEventOptional = this.gradStatusEventRepository.findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString());
    final GradStatusEvent gradStatusEvent;
    if(gradStatusEventOptional.isEmpty()) {
      log.debug(NO_RECORD_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      ArchiveStudentsSagaData sagaData = JsonUtil.getJsonObjectFromString(ArchiveStudentsSagaData.class, event.getEventPayload());
      var numStudentsArchived = this.graduationStatusService.archiveStudents(sagaData.getBatchId(), sagaData.getSchoolsOfRecords(), sagaData.getStudentStatusCode(), sagaData.getUpdateUser());
      event.setEventPayload(String.valueOf(numStudentsArchived));
      event.setEventOutcome(STUDENTS_ARCHIVED);
      gradStatusEvent = this.createGradStatusEventRecord(event);
    } else {
      log.debug(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      gradStatusEvent = gradStatusEventOptional.get();
      gradStatusEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
    }
    return createResponseEvent(this.gradStatusEventRepository.save(gradStatusEvent));
  }

  private GradStatusEvent createGradStatusEventRecord(final Event event) {
    return GradStatusEvent.builder()
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser(event.getEventType().toString())
            .updateUser(event.getEventType().toString())
            .eventPayload(event.getEventPayload())
            .eventType(event.getEventType().toString())
            .sagaId(event.getSagaId())
            .eventStatus(MESSAGE_PUBLISHED.toString())
            .eventOutcome(String.valueOf(event.getEventOutcome()))
            .replyChannel(event.getReplyTo())
            .build();
  }

  private byte[] createResponseEvent(GradStatusEvent event) throws JsonProcessingException {
    var responseEvent = Event.builder()
            .sagaId(event.getSagaId())
            .eventType(EventType.valueOf(event.getEventType()))
            .eventOutcome(EventOutcome.valueOf(event.getEventOutcome()))
            .eventPayload(event.getEventPayload()).build();
    return JsonUtil.getJsonBytesFromObject(responseEvent);
  }
}
