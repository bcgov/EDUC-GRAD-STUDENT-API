package ca.bc.gov.educ.api.gradstudent.orchestrator;

import ca.bc.gov.educ.api.gradstudent.messaging.MessagePublisher;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.ArchiveStudentsSagaData;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEventStatesEntity;
import ca.bc.gov.educ.api.gradstudent.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.gradstudent.service.SagaService;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.gradstudent.constant.EventOutcome.BATCH_API_NOTIFIED;
import static ca.bc.gov.educ.api.gradstudent.constant.EventOutcome.STUDENTS_ARCHIVED;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.ARCHIVE_STUDENTS;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.NOTIFY_ARCHIVE_STUDENT_BATCH_COMPLETED;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaEnum.ARCHIVE_STUDENTS_SAGA;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.gradstudent.constant.Topics.*;

@Component
@Slf4j
public class ArchiveStudentsOrchestrator extends BaseOrchestrator<ArchiveStudentsSagaData> {

  protected ArchiveStudentsOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher) {
    super(sagaService, messagePublisher, ArchiveStudentsSagaData.class, ARCHIVE_STUDENTS_SAGA.toString(), GRAD_STUDENT_ARCHIVE_STUDENTS_SAGA_TOPIC.toString());
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(ARCHIVE_STUDENTS, this::archiveStudents)
      .step(ARCHIVE_STUDENTS, STUDENTS_ARCHIVED, NOTIFY_ARCHIVE_STUDENT_BATCH_COMPLETED, this::notifyBatchApi)
      .end(NOTIFY_ARCHIVE_STUDENT_BATCH_COMPLETED, BATCH_API_NOTIFIED);
  }

  protected void archiveStudents(final Event event, final SagaEntity saga, final ArchiveStudentsSagaData sagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(ARCHIVE_STUDENTS.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(ARCHIVE_STUDENTS)
            .eventPayload(JsonUtil.getJsonStringFromObject(sagaData))
            .replyTo(this.getTopicToSubscribe())
            .batchId(String.valueOf(sagaData.getBatchId()))
            .build();
    this.postMessageToTopic(GRAD_STUDENT_API_TOPIC.toString(), nextEvent);
    log.info("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }

  protected void notifyBatchApi(final Event event, final SagaEntity saga, final ArchiveStudentsSagaData sagaData) {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(NOTIFY_ARCHIVE_STUDENT_BATCH_COMPLETED.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event nextEvent = Event.builder()
            .sagaId(saga.getSagaId())
            .replyTo(this.getTopicToSubscribe())
            .eventType(NOTIFY_ARCHIVE_STUDENT_BATCH_COMPLETED)
            .batchId(String.valueOf(sagaData.getBatchId()))
            .build();
    this.postMessageToTopic(String.valueOf(GRAD_BATCH_API_TOPIC), nextEvent);
    log.debug("message sent to {} for {} Event. :: {}", this.getTopicToSubscribe(), nextEvent, saga.getSagaId());
  }
}
