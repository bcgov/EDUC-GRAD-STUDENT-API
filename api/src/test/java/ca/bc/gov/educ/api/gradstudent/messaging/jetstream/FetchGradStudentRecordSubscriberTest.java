package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GradStudentRecord;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FetchGradStudentRecordSubscriberTest {

    @Mock Connection connection;
    @Mock Dispatcher dispatcher;
    @Mock GradStudentService gradStudentService;
    @Mock EducGradStudentApiConstants constants;

    @InjectMocks
    private FetchGradStudentRecordSubscriber subscriber;

    @BeforeEach
    void setUp() {
        when(connection.createDispatcher(eq(subscriber))).thenReturn(dispatcher);
        subscriber.subscribe();
    }

    @Test
    void testOnMessage_WhenStudentRecordFound_DoesNotThrow() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        GradStudentRecord record = new GradStudentRecord(
                studentID, "Program", Date.valueOf("2023-01-01"),
                UUID.randomUUID(), UUID.randomUUID(), "Y", "12"
        );
        when(gradStudentService.getGraduationStudentRecord(studentID)).thenReturn(record);
        when(gradStudentService.parseGraduationStatus(record.toString())).thenReturn(true);

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    void testOnMessage_WhenStudentNotFound_DoesNotThrow() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        when(gradStudentService.getGraduationStudentRecord(studentID))
                .thenThrow(new EntityNotFoundException());

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    void testOnMessage_WhenGeneralExceptionOccurs_DoesNotThrow() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        when(gradStudentService.getGraduationStudentRecord(studentID))
                .thenThrow(new RuntimeException("Boom"));

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }
}