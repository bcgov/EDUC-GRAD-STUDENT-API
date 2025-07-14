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
import io.nats.client.MessageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FetchGradStudentRecordSubscriberUnitTest {

    @Mock Connection connection;
    @Mock Dispatcher dispatcher;
    @Mock GradStudentService gradStudentService;
    @Mock EducGradStudentApiConstants constants;

    @InjectMocks
    private FetchGradStudentRecordSubscriber subscriber;

    @BeforeEach
    void init() {
        when(connection.createDispatcher(any(MessageHandler.class)))
                .thenReturn(dispatcher);
        subscriber.subscribe();
    }

    private Message buildMsg(UUID studentId) throws JsonProcessingException {
        Event e = Event.builder().eventPayload(studentId.toString()).build();
        byte[] data = JsonUtil.getJsonBytesFromObject(e);
        Message msg = mock(Message.class);
        when(msg.getData()).thenReturn(data);
        when(msg.getReplyTo()).thenReturn("reply");
        return msg;
    }

    @Test
    void whenRecordExists_thenNoException() throws Exception {
        UUID id = UUID.randomUUID();
        Message msg = buildMsg(id);

        GradStudentRecord rec = new GradStudentRecord(
                id, "Prog", Date.valueOf("2023-01-01"),
                UUID.randomUUID(), UUID.randomUUID(), "Y", "12"
        );
        when(gradStudentService.getGraduationStudentRecord(id)).thenReturn(rec);
        when(gradStudentService.parseGraduationStatus(rec.toString())).thenReturn(true);

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    void whenEntityNotFound_thenNoException() throws Exception {
        UUID id = UUID.randomUUID();
        Message msg = buildMsg(id);
        when(gradStudentService.getGraduationStudentRecord(id))
                .thenThrow(new EntityNotFoundException());

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    void whenRuntimeException_thenNoException() throws Exception {
        UUID id = UUID.randomUUID();
        Message msg = buildMsg(id);
        when(gradStudentService.getGraduationStudentRecord(id))
                .thenThrow(new RuntimeException("boom"));

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }
}
