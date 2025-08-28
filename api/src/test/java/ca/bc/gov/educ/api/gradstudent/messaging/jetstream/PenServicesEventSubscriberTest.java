package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.penservices.v1.StudentMerge;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
public class PenServicesEventSubscriberTest extends BaseIntegrationTest {

    @Mock
    private Message mockMessage;

    @MockBean
    private GradStudentService gradStudentService;

    // NATS
    @MockBean
    private Connection connection;
    @MockBean
    private NatsConnection natsConnection;
    @MockBean
    private Publisher publisher;
    @MockBean
    private Subscriber subscriber;

    @MockBean
    private EducGradStudentApiConstants constants;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        openMocks(this);
        when(natsConnection.connection()).thenReturn(connection);

        Field natsConnectionField = Subscriber.class.getDeclaredField("natsConnection");
        natsConnectionField.setAccessible(true);
        natsConnectionField.set(subscriber, connection);
    }

    @Test
    public void testOnMessage_Success() throws JsonProcessingException {
        UUID eventID = UUID.randomUUID();
        UUID studentID = UUID.randomUUID();
        UUID mergeStudentID = UUID.randomUUID();
        Event event = prepareEventMessage(eventID, studentID, mergeStudentID);
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());
        when(mockMessage.getReplyTo()).thenReturn("replyTo");
        assertDoesNotThrow(() -> subscriber.onMessage(mockMessage));
    }

    private Event prepareEventMessage(UUID eventID, UUID studentID, UUID mergeStudentID) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return Event.builder()
                .eventPayload(objectMapper.writeValueAsString(createChoreographyEvent(eventID, studentID, mergeStudentID)))
                .build();
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
}