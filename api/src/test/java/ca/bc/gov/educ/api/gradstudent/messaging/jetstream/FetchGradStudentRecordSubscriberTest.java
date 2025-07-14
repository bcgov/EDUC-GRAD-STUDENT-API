package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GradStudentRecord;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.sql.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FetchGradStudentRecordSubscriberTest extends BaseIntegrationTest {

    @Mock
    private Message mockNatsMessage;

    @InjectMocks
    private FetchGradStudentRecordSubscriber fetchGradStudentRecordSubscriber;

    @MockBean
    private GradStudentService gradStudentService;

    @Before
    public void setUp() throws Exception {
        openMocks(this);
        when(natsConnection.connection()).thenReturn(connection);

        Field gradStudentServiceField = FetchGradStudentRecordSubscriber.class.getDeclaredField("gradStudentService");
        gradStudentServiceField.setAccessible(true);
        gradStudentServiceField.set(fetchGradStudentRecordSubscriber, gradStudentService);

        Field natsConnectionField = FetchGradStudentRecordSubscriber.class.getDeclaredField("natsConnection");
        natsConnectionField.setAccessible(true);
        natsConnectionField.set(fetchGradStudentRecordSubscriber, natsConnection.connection());
    }

    @Test
    public void testOnMessage_WhenStudentRecordFound_PublishesResponse() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        byte[] eventPayload = JsonUtil.getJsonBytesFromObject(event);
        when(mockNatsMessage.getData()).thenReturn(eventPayload);

        GradStudentRecord mockRecord = new GradStudentRecord(UUID.randomUUID(), "Program", Date.valueOf("2023-01-01"), UUID.randomUUID(), UUID.randomUUID(), "Y", "12");
        mockRecord.setStudentID(studentID);
        mockRecord.setSchoolOfRecordId(UUID.randomUUID());
        when(gradStudentService.getGraduationStudentRecord(studentID)).thenReturn(mockRecord);
        when(gradStudentService.parseGraduationStatus(any())).thenReturn(Boolean.TRUE);

        assertDoesNotThrow(() -> fetchGradStudentRecordSubscriber.onMessage(mockNatsMessage));
    }

    @Test
    public void testOnMessage_WhenStudentNotFound_PublishesErrorResponse() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        byte[] eventPayload = JsonUtil.getJsonBytesFromObject(event);
        when(mockNatsMessage.getData()).thenReturn(eventPayload);

        when(gradStudentService.getGraduationStudentRecord(studentID)).thenThrow(new EntityNotFoundException());

        assertDoesNotThrow(() -> fetchGradStudentRecordSubscriber.onMessage(mockNatsMessage));
    }

    @Test
    public void testOnMessage_WhenGeneralExceptionOccurs_PublishesErrorResponse() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        byte[] eventPayload = JsonUtil.getJsonBytesFromObject(event);
        when(mockNatsMessage.getData()).thenReturn(eventPayload);

        when(gradStudentService.getGraduationStudentRecord(studentID)).thenThrow(new RuntimeException("Unexpected error"));

        assertDoesNotThrow(() -> fetchGradStudentRecordSubscriber.onMessage(mockNatsMessage));
    }
}