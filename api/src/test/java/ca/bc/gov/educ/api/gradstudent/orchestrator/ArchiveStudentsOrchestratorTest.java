package ca.bc.gov.educ.api.gradstudent.orchestrator;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.messaging.MessagePublisher;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.ArchiveStudentsSagaData;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEntity;
import ca.bc.gov.educ.api.gradstudent.repository.SagaEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.SagaRepository;
import ca.bc.gov.educ.api.gradstudent.service.SagaService;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.gradstudent.constant.EventOutcome.*;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.*;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaEnum.ARCHIVE_STUDENTS_SAGA;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaStatusEnum.COMPLETED;
import static ca.bc.gov.educ.api.gradstudent.constant.Topics.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ArchiveStudentsOrchestratorTest extends BaseIntegrationTest {

  @Autowired
  SagaRepository repository;
  @Autowired
  SagaEventRepository sagaEventRepository;
  @Autowired
  private SagaService sagaService;
  @Autowired
  private MessagePublisher messagePublisher;
  @Autowired
  private ArchiveStudentsOrchestrator orchestrator;

  private SagaEntity saga;
  private ArchiveStudentsSagaData sagaData;

  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  String sagaPayload;

  long batchId = 123456;

  @BeforeEach
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
    sagaData = getArchiveStudentsSagaData();
    sagaPayload = JsonUtil.getJsonStringFromObject(sagaData);
    saga = sagaService.createSagaRecordInDB(ARCHIVE_STUDENTS_SAGA.toString(), "Test",
            sagaPayload, batchId);
  }

  @AfterEach
  public void after() {
    sagaEventRepository.deleteAll();
    repository.deleteAll();
  }

  @Test
  void testArchiveStudents_givenEventAndSagaData_shouldPostEventToApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(INITIATED)
            .eventOutcome(INITIATE_SUCCESS)
            .sagaId(saga.getSagaId())
            .batchId(String.valueOf(batchId))
            .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(GRAD_STUDENT_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(ARCHIVE_STUDENTS);
    var archiveStudentsEvent = JsonUtil.getJsonObjectFromString(ArchiveStudentsSagaData.class, newEvent.getEventPayload());
    assertThat(archiveStudentsEvent).isEqualTo(sagaData);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(ARCHIVE_STUDENTS.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates).hasSize(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(INITIATE_SUCCESS.toString());
  }

  @Test
  void testNotifyBatchApi_givenEventAndSagaData_shouldPostEventToApi() throws IOException, InterruptedException, TimeoutException {
    var invocations = mockingDetails(messagePublisher).getInvocations().size();
    var event = Event.builder()
            .eventType(ARCHIVE_STUDENTS)
            .eventOutcome(STUDENTS_ARCHIVED)
            .sagaId(saga.getSagaId())
            .batchId(String.valueOf(batchId))
            .build();

    orchestrator.handleEvent(event);
    verify(messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(GRAD_BATCH_API_TOPIC.toString()), eventCaptor.capture());
    var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(NOTIFY_ARCHIVE_STUDENT_BATCH_COMPLETED);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(NOTIFY_ARCHIVE_STUDENT_BATCH_COMPLETED.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates).hasSize(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(ARCHIVE_STUDENTS.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(STUDENTS_ARCHIVED.toString());
  }

  @Test
  void testNotifyBatchApiResponse_givenEventAndSagaData_shouldCompleteSaga() throws IOException, InterruptedException, TimeoutException {
    var event = Event.builder()
            .eventType(NOTIFY_ARCHIVE_STUDENT_BATCH_COMPLETED)
            .eventOutcome(BATCH_API_NOTIFIED)
            .sagaId(saga.getSagaId())
            .batchId(String.valueOf(batchId))
            .build();

    orchestrator.handleEvent(event);

    var sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(COMPLETED.toString());
    var sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates).hasSize(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(NOTIFY_ARCHIVE_STUDENT_BATCH_COMPLETED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(BATCH_API_NOTIFIED.toString());
  }
}
