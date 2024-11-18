package ca.bc.gov.educ.api.gradstudent.scheduler;

import ca.bc.gov.educ.api.gradstudent.EducGradStudentApiApplication;
import ca.bc.gov.educ.api.gradstudent.constant.SagaStatusEnum;
import ca.bc.gov.educ.api.gradstudent.model.entity.SagaEntity;
import ca.bc.gov.educ.api.gradstudent.repository.SagaEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.SagaRepository;
import lombok.val;
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import java.util.ArrayList;

import static ca.bc.gov.educ.api.gradstudent.constant.EventType.INITIATED;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaEnum.ARCHIVE_STUDENTS_SAGA;
import static ca.bc.gov.educ.api.gradstudent.constant.SagaStatusEnum.STARTED;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = {EducGradStudentApiApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
class EventTaskSchedulerTest {

    @Autowired
    EventTaskScheduler eventTaskScheduler;

    @Autowired
    SagaRepository sagaRepository;

    @Autowired
    SagaEventRepository sagaEventRepository;

    private static final String PAYLOAD_STR = """
             {
                "createUser": "test",
                "updateUser": "test",
                "batchID": "123456",
                "studentStatusCode": "CUR"
              }\
            """;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        final var statuses = new ArrayList<String>();
        statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
        statuses.add(SagaStatusEnum.STARTED.toString());
        this.eventTaskScheduler.setStatusFilters(statuses);
    }

    @AfterEach
    void cleanup(){
        sagaEventRepository.deleteAll();
        sagaRepository.deleteAll();
    }

    @Test
    void testFindAndProcessUncompletedSagas_givenSagaRecordInSTARTEDStateForMoreThan5Minutes_shouldBeProcessed() {
        final SagaEntity placeHolderRecord = this.createDummySagaRecord(ARCHIVE_STUDENTS_SAGA.toString());
        this.sagaRepository.save(placeHolderRecord);
        LockAssert.TestHelper.makeAllAssertsPass(true);
        this.eventTaskScheduler.findAndProcessUncompletedSagas();
        val sagaId = placeHolderRecord.getSagaId();
        val updatedRecordFromDB = this.sagaRepository.findById(sagaId);
        assertThat(updatedRecordFromDB).isPresent();
        assertThat(updatedRecordFromDB.get().getRetryCount()).isNotNull().isPositive();
        final var eventStates = this.sagaEventRepository.findBySaga(placeHolderRecord);
        assertThat(eventStates).isNotEmpty();
    }


    private SagaEntity createDummySagaRecord(final String sagaName) {
        return SagaEntity
                .builder()
                .payload(PAYLOAD_STR)
                .sagaName(sagaName)
                .status(STARTED.toString())
                .sagaState(INITIATED.toString())
                .createDate(LocalDateTime.now().minusMinutes(3))
                .createUser("test")
                .updateUser("test")
                .updateDate(LocalDateTime.now().minusMinutes(10))
                .build();
    }
}