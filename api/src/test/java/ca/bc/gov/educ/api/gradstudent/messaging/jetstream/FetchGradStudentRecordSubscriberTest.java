package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GradStudentRecord;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FetchGradStudentRecordSubscriberTest extends BaseIntegrationTest {

    @MockBean
    private FetchGradStudentRecordSubscriber ignoredDummySubscriber;

    @MockBean
    private GradStudentService gradStudentService;

    @MockBean
    private EducGradStudentApiConstants constants;

    @MockBean
    private Dispatcher dispatcher;

    private FetchGradStudentRecordSubscriber subscriber;

    @Before
    public void setUp() {
        subscriber = new FetchGradStudentRecordSubscriber(connection, gradStudentService, constants);

        when(connection.createDispatcher(any(MessageHandler.class)))
                .thenReturn(dispatcher);

        subscriber.subscribe();
    }

    @Test
    public void testOnMessage_WhenStudentRecordFound_PublishesResponse() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = org.mockito.Mockito.mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        GradStudentRecord record = new GradStudentRecord(
                studentID, "Program", Date.valueOf("2023-01-01"),
                UUID.randomUUID(), UUID.randomUUID(), "Y", "12"
        );
        when(gradStudentService.getGraduationStudentRecord(studentID)).thenReturn(record);
        when(gradStudentService.parseGraduationStatus(any())).thenReturn(true);

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    public void testOnMessage_WhenStudentNotFound_PublishesErrorResponse() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = org.mockito.Mockito.mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        when(gradStudentService.getGraduationStudentRecord(studentID))
                .thenThrow(new EntityNotFoundException());

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    public void testOnMessage_WhenGeneralExceptionOccurs_PublishesErrorResponse() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = org.mockito.Mockito.mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        when(gradStudentService.getGraduationStudentRecord(studentID))
                .thenThrow(new RuntimeException("Boom"));

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }
}
