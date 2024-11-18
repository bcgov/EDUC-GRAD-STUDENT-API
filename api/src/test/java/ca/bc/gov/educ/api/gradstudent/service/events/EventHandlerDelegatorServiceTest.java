package ca.bc.gov.educ.api.gradstudent.service.events;

import ca.bc.gov.educ.api.gradstudent.constant.StudentStatusCodes;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.messaging.MessagePublisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.support.NatsMessageImpl;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.junit.After;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.constant.EventOutcome.*;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.ARCHIVE_STUDENTS;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.ARCHIVE_STUDENTS_REQUEST;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaEnum.ARCHIVE_STUDENTS_SAGA;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.gradstudent.constant.Topics.GRAD_BATCH_API_TOPIC;
import static ca.bc.gov.educ.api.gradstudent.constant.Topics.GRAD_STUDENT_ARCHIVE_STUDENTS_SAGA_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EventHandlerDelegatorServiceTest extends BaseIntegrationTest {
  @Autowired
  MessagePublisher messagePublisher;
  @Autowired
  Publisher publisher;

  @MockBean
  Connection connection;

  @Autowired
  EventHandlerDelegatorService eventHandlerDelegatorService;

  @Captor
  ArgumentCaptor<byte[]> eventCaptor;
  @Autowired
  SagaRepository sagaRepository;
  @Autowired
  SagaEventRepository sagaEventRepository;
  @Autowired
  GradStatusEventRepository gradStatusEventRepository;
  @SpyBean
  GraduationStudentRecordRepository gradStudentRepository;
  @Autowired
  GraduationStudentRecordHistoryRepository studentRecordHistoryRepository;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void after() {
    this.gradStatusEventRepository.deleteAll();
    this.sagaEventRepository.deleteAll();
    this.sagaRepository.deleteAll();
    this.gradStudentRepository.deleteAll();
    this.studentRecordHistoryRepository.deleteAll();
  }

  @Test
  public void testHandleArchiveStudentsRequestEvent_givenValidPayload_whenSuccessfullyProcessed_shouldReturnSuccess() throws IOException {
    var studentRecord1 = createMockGraduationStudentRecord();
    var studentRecord2 = createMockGraduationStudentRecord();
    var studentRecord3 = createMockGraduationStudentRecord();
    var studentRecord4 = createMockGraduationStudentRecord();
    studentRecord4.setStudentStatus(StudentStatusCodes.DECEASED.getCode());
    this.gradStudentRepository.saveAll(Arrays.asList(studentRecord1, studentRecord2, studentRecord3, studentRecord4));

    var payload = getArchiveStudentsSagaData();
    final Event event = Event.builder()
            .eventType(ARCHIVE_STUDENTS_REQUEST)
            .replyTo(String.valueOf(GRAD_BATCH_API_TOPIC))
            .eventPayload(JsonUtil.getJsonStringFromObject(payload))
            .build();
    final Message message = NatsMessageImpl.builder()
            .connection(this.connection)
            .data(JsonUtil.getJsonBytesFromObject(event))
            .SID("SID")
            .replyTo("TEST_TOPIC")
            .build();
    this.eventHandlerDelegatorService.handleEvent(event, message);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(any(), this.eventCaptor.capture());
    final var replyEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    var createdSagas = this.sagaRepository.findAll();

    assertThat(replyEvent.getSagaId()).isNull();
    assertThat(replyEvent.getEventType()).isEqualTo(ARCHIVE_STUDENTS_REQUEST);
    assertThat(replyEvent.getEventOutcome()).isEqualTo(ARCHIVE_STUDENTS_STARTED);
    assertThat(replyEvent.getEventPayload()).isEqualTo("3");

    assertThat(createdSagas).isNotEmpty().size().isEqualTo(1);
    assertThat(createdSagas.get(0).getSagaState()).isEqualTo(String.valueOf(ARCHIVE_STUDENTS));
    assertThat(createdSagas.get(0).getStatus()).isEqualTo(String.valueOf(IN_PROGRESS));
    assertThat(createdSagas.get(0).getSagaName()).isEqualTo(String.valueOf(ARCHIVE_STUDENTS_SAGA));
  }

  @Test
  public void testHandleArchiveStudentsRequestEvent_givenArchiveAlreadyInProgress_whenSuccessfullyProcessed_shouldReturnCONFLICT() throws IOException {
    var payload = getArchiveStudentsSagaData();
    final Event event = Event.builder()
            .eventType(ARCHIVE_STUDENTS_REQUEST)
            .replyTo(String.valueOf(GRAD_BATCH_API_TOPIC))
            .eventPayload(JsonUtil.getJsonStringFromObject(payload))
            .build();
    final Message message = NatsMessageImpl.builder()
            .connection(this.connection)
            .data(JsonUtil.getJsonBytesFromObject(event))
            .SID("SID")
            .replyTo("TEST_TOPIC")
            .build();
    var saga = createMockSaga();
    saga.setPayload(JsonUtil.getJsonStringFromObject(payload));
    this.sagaRepository.save(saga);
    this.eventHandlerDelegatorService.handleEvent(event, message);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(any(), this.eventCaptor.capture());
    final var replyEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));

    assertThat(replyEvent.getSagaId()).isNull();
    assertThat(replyEvent.getEventType()).isEqualTo(ARCHIVE_STUDENTS_REQUEST);
    assertThat(replyEvent.getEventOutcome()).isEqualTo(FAILED_TO_START_ARCHIVE_STUDENTS_SAGA);
    assertThat(replyEvent.getEventPayload()).isEqualTo("CONFLICT");
  }

  @Test
  public void testHandleArchiveStudentsEvent_givenValidPayload_whenSuccessfullyProcessed_shouldSendStudentArchivedEvent() throws IOException {
    var payload = getArchiveStudentsSagaData();

    UUID sagaId = UUID.randomUUID();
    final Event event = Event.builder()
            .eventType(ARCHIVE_STUDENTS)
            .replyTo(String.valueOf(GRAD_STUDENT_ARCHIVE_STUDENTS_SAGA_TOPIC))
            .eventPayload(JsonUtil.getJsonStringFromObject(payload))
            .sagaId(sagaId)
            .batchId("123456")
            .build();
    final Message message = NatsMessageImpl.builder()
            .connection(this.connection)
            .data(JsonUtil.getJsonBytesFromObject(event))
            .SID("SID")
            .replyTo("TEST_TOPIC")
            .build();

    var studentRecord1 = createMockGraduationStudentRecord();
    var studentRecord2 = createMockGraduationStudentRecord();
    var studentRecord3 = createMockGraduationStudentRecord();
    var studentRecord4 = createMockGraduationStudentRecord();
    studentRecord4.setStudentStatus(StudentStatusCodes.DECEASED.getCode());
    this.gradStudentRepository.saveAll(Arrays.asList(studentRecord1, studentRecord2, studentRecord3, studentRecord4));

    doReturn(3).when(gradStudentRepository).archiveStudents(any(), any(), anyLong(), any());

    this.eventHandlerDelegatorService.handleEvent(event, message);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(any(), this.eventCaptor.capture());
    final var replyEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(replyEvent.getSagaId()).isEqualTo(sagaId);
    assertThat(replyEvent.getEventType()).isEqualTo(ARCHIVE_STUDENTS);
    assertThat(replyEvent.getEventOutcome()).isEqualTo(STUDENTS_ARCHIVED);
    assertThat(replyEvent.getEventPayload()).isEqualTo("3");
    assertThat(studentRecordHistoryRepository.findAll()).hasSize(3);
  }
}
