package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventStatus;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.penservices.v1.StudentMerge;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.service.event.PenServicesEventHandlerDelegatorService;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.Message;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PenServicesEventHandlerDelegatorServiceTest extends BaseIntegrationTest {

    @Autowired
    PenServicesEventHandlerDelegatorService penServicesEventHandlerDelegatorService;
    @Autowired
    GradStatusEventRepository gradStatusEventRepository;

    @After
    public void afterEach() {
        this.gradStatusEventRepository.deleteAll();
    }

    @Before
    public void beforeEach() {
        this.gradStatusEventRepository.deleteAll();
    }

    @Test
    public void handleChoreographyEvent_givenCREATE_MERGE_EVENTAndValidPayload_shouldSendEmail() throws IOException, InterruptedException {
        val eventID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID mergeStudentID = UUID.randomUUID();
        val message = Mockito.mock(Message.class);
        doNothing().when(message).ack();
        ChoreographedEvent choreographyEvent = this.createChoreographyEvent(eventID, studentID, mergeStudentID);
        when(message.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(choreographyEvent));
        this.penServicesEventHandlerDelegatorService.handleChoreographyEvent(choreographyEvent, message);
        this.waitForAsyncToFinish(eventID);
        this.penServicesEventHandlerDelegatorService.handleChoreographyEvent(this.createChoreographyEvent(eventID, studentID, mergeStudentID), message);
        verify(message, atLeast(2)).ack();
    }

    private void waitForAsyncToFinish(final UUID eventID) throws InterruptedException {
        int i = 0;
        while (true) {
            if (i >= 50) {
                break; // break out after trying for 5 seconds.
            }
            val event = this.gradStatusEventRepository.findByEventId(eventID);
            if (event.isPresent() && EventStatus.PROCESSED.toString().equalsIgnoreCase(event.get().getEventStatus())) {
                break;
            }
            TimeUnit.MILLISECONDS.sleep(50);
            i++;
        }
    }

    private ChoreographedEvent createChoreographyEvent(UUID eventID, UUID studentID, UUID mergeStudentID) throws JsonProcessingException {
        final ChoreographedEvent choreographedEvent = new ChoreographedEvent();
        choreographedEvent.setEventType(EventType.CREATE_MERGE);
        choreographedEvent.setEventOutcome(EventOutcome.MERGE_CREATED);
        choreographedEvent.setEventPayload(JsonUtil.getJsonStringFromObject(this.createStudentMergePayload(studentID, mergeStudentID)));
        choreographedEvent.setEventID(eventID.toString());
        choreographedEvent.setCreateUser("TEST");
        choreographedEvent.setUpdateUser("TEST");
        return choreographedEvent;
    }

    private List<StudentMerge> createStudentMergePayload(UUID studentID, UUID mergeStudentID) {
        final List<StudentMerge> studentMerges = new ArrayList<>();
        final StudentMerge merge = new StudentMerge();
        merge.setStudentID(studentID.toString());
        merge.setMergeStudentID(mergeStudentID.toString());
        merge.setStudentMergeDirectionCode("TO");
        merge.setStudentMergeSourceCode("MI");
        studentMerges.add(merge);
        return studentMerges;
    }

    @SneakyThrows
    private Optional<GradStatusEvent> getGradStatusEvent(ChoreographedEvent choreographedEvent) {
        return Optional.of(GradStatusEvent
                .builder()
                .eventType(EventType.CREATE_MERGE.name())
                .eventPayload(JsonUtil.getJsonStringFromObject(choreographedEvent))
                .eventOutcome(EventOutcome.MERGE_CREATED.name())
                .build());
    }


}
