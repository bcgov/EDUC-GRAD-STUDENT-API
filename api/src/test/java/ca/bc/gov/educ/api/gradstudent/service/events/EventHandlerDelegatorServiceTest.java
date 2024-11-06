package ca.bc.gov.educ.api.gradstudent.service.events;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.messaging.MessagePublisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.SagaEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.SagaRepository;
import ca.bc.gov.educ.api.gradstudent.support.NatsMessageImpl;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static ca.bc.gov.educ.api.gradstudent.constant.EventType.ARCHIVE_STUDENTS_REQUEST;
import static ca.bc.gov.educ.api.gradstudent.constant.Topics.GRAD_BATCH_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

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

  @MockBean
  GradStatusEventRepository gradStatusEventRepository;

  @BeforeEach
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void after() {
    this.gradStatusEventRepository.deleteAll();
    this.sagaEventRepository.deleteAll();
    this.sagaRepository.deleteAll();
  }

  @Test
  public void testHandleArchiveStudentsRequestEvent_givenValidPayload_whenSuccessfullyProcessed_shouldReturnSuccess() throws IOException {
    var payload = getArchiveStudentsSagaData();
    var expectedResponse = "SUCCESS".getBytes(StandardCharsets.UTF_8);
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
    final var replyEvent = this.eventCaptor.getValue();

    assertThat(replyEvent).isNotNull().isEqualTo(expectedResponse);
  }

  @Test
  public void testHandleArchiveStudentsRequestEvent_givenArchiveAlreadyInProgress_whenSuccessfullyProcessed_shouldReturnCONFLICT() throws IOException {
    var payload = getArchiveStudentsSagaData();
    var expectedResponse = "CONFLICT".getBytes(StandardCharsets.UTF_8);
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
    final var replyEvent = this.eventCaptor.getValue();

    assertThat(replyEvent).isNotNull().isEqualTo(expectedResponse);
  }
}
