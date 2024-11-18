package ca.bc.gov.educ.api.gradstudent.scheduler;

import ca.bc.gov.educ.api.gradstudent.EducGradStudentApiApplication;
import ca.bc.gov.educ.api.gradstudent.constant.EventStatus;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEventStatesEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.SagaEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.SagaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static ca.bc.gov.educ.api.gradstudent.constant.SagaStatusEnum.COMPLETED;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {EducGradStudentApiApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PurgeOldRecordsSchedulerTest {

  @Autowired
  SagaRepository repository;

  @Autowired
  SagaEventRepository sagaEventRepository;

  @Autowired
  GradStatusEventRepository gradStatusEventRepository;

  @Autowired
  PurgeOldRecordsScheduler purgeOldRecordsScheduler;


  @Test
  void testPurgeOldRecords_givenOldRecordsPresent_shouldBeDeleted() {
    final String batchId = "123456";
    final var payload = " {\n" +
        "    \"createUser\": \"test\",\n" +
        "    \"updateUser\": \"test\",\n" +
        "    \"batchID\": \"" + batchId + "\",\n" +
        "    \"studentStatusCode\": \"CUR\"\n" +
        "  }";
    final var saga_today = this.getSaga(payload, LocalDateTime.now());
    final var yesterday = LocalDateTime.now().minusDays(1);
    final var saga_yesterday = this.getSaga(payload, yesterday);

    this.repository.save(saga_today);
    this.sagaEventRepository.save(this.getSagaEvent(saga_today, payload));
    this.gradStatusEventRepository.save(this.getServicesEvent(saga_today, payload, LocalDateTime.now()));

    this.repository.save(saga_yesterday);
    this.sagaEventRepository.save(this.getSagaEvent(saga_yesterday, payload));
    this.gradStatusEventRepository.save(this.getServicesEvent(saga_yesterday, payload, yesterday));

    this.purgeOldRecordsScheduler.purgeOldRecords();
    final var sagas = this.repository.findAll();
    assertThat(sagas).hasSize(1);

    final var sagaEvents = this.sagaEventRepository.findAll();
    assertThat(sagaEvents).hasSize(1);

    final var servicesEvents = this.gradStatusEventRepository.findAll();
    assertThat(servicesEvents).hasSize(1);
  }


  private SagaEntity getSaga(final String payload, final LocalDateTime createDateTime) {
    return SagaEntity
        .builder()
        .payload(payload)
        .sagaName("ARCHIVE_STUDENTS_SAGA")
        .status(COMPLETED.toString())
        .sagaState(COMPLETED.toString())
        .createDate(createDateTime)
        .createUser("GRAD_API")
        .updateUser("GRAD_API")
        .updateDate(createDateTime)
        .build();
  }

  private SagaEventStatesEntity getSagaEvent(final SagaEntity saga, final String payload) {
    return SagaEventStatesEntity
        .builder()
        .sagaEventResponse(payload)
        .saga(saga)
        .sagaEventState("ARCHIVE_STUDENTS")
        .sagaStepNumber(3)
        .sagaEventOutcome("STUDENTS_ARCHIVED")
        .createDate(LocalDateTime.now())
        .createUser("GRAD_API")
        .updateUser("GRAD_API")
        .updateDate(LocalDateTime.now())
        .build();
  }

  private GradStatusEvent getServicesEvent(final SagaEntity saga, final String payload, final LocalDateTime createDateTime) {
    return GradStatusEvent
      .builder()
      .eventPayloadBytes(payload.getBytes())
      .eventStatus(EventStatus.MESSAGE_PUBLISHED.toString())
      .eventType("ARCHIVE_STUDENTS")
      .sagaId(saga.getSagaId())
      .eventOutcome("STUDENTS_ARCHIVED")
      .replyChannel("TEST_CHANNEL")
      .createDate(createDateTime)
      .createUser("GRAD_API")
      .updateUser("GRAD_API")
      .updateDate(createDateTime)
      .build();
  }
}
