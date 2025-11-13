package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GradStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nats.client.Connection;
import io.nats.client.Message;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FetchGradStudentRecordSubscriberTest extends BaseIntegrationTest {

    @Mock
    private Message mockMessage;

    @InjectMocks
    private FetchGradStudentRecordSubscriber fetchSubscriber;

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

        Field gradStudentServiceField = FetchGradStudentRecordSubscriber.class.getDeclaredField("gradStudentService");
        gradStudentServiceField.setAccessible(true);
        gradStudentServiceField.set(fetchSubscriber, gradStudentService);

        Field natsConnectionField = FetchGradStudentRecordSubscriber.class.getDeclaredField("natsConnection");
        natsConnectionField.setAccessible(true);
        natsConnectionField.set(fetchSubscriber, connection);
    }

    @Test
    public void testOnMessage_Success() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = prepareEventMessage(studentID);
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());
        when(mockMessage.getReplyTo()).thenReturn("replyTo");

        GradStudentRecord rec = new GradStudentRecord(
                studentID, "Prog", Date.valueOf("2023-01-01"),
                UUID.randomUUID(), UUID.randomUUID(), "Y", "12", "10",
                "11"
        );
        when(gradStudentService.getGraduationStudentRecord(studentID)).thenReturn(rec);
        when(gradStudentService.parseGraduationStatus(anyString())).thenReturn(true);

        assertDoesNotThrow(() -> fetchSubscriber.onMessage(mockMessage));
    }

    @Test
    public void testOnMessage_EntityNotFound() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = prepareEventMessage(studentID);
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());
        when(mockMessage.getReplyTo()).thenReturn("replyTo");

        when(gradStudentService.getGraduationStudentRecord(studentID))
                .thenThrow(new EntityNotFoundException());

        assertDoesNotThrow(() -> fetchSubscriber.onMessage(mockMessage));
    }

    @Test
    public void test_GenerateResponse() throws IOException {
        UUID studentID = UUID.randomUUID();
        String gradData = new String(Files.readAllBytes(Paths.get("src/test/resources/json/studentGradCourseData.json")));
        GradStudentRecord rec = new GradStudentRecord(studentID, "2018-EN", new java.util.Date(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "studentStatusCode",
                "{\"nonGradReasons\":null,\"graduated\":true}",
                "10",
                gradData
        );

        String result = fetchSubscriber.getResponse(rec);
        assertNotNull(result);
    }

    @Test
    public void testGetTopicName() {
        assertThat(fetchSubscriber.getTopic())
                .isEqualTo(Topics.GRAD_STUDENT_API_FETCH_GRAD_STUDENT_TOPIC.toString());
    }

    private Event prepareEventMessage(UUID studentID) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return Event.builder()
                .eventPayload(objectMapper.writeValueAsString(studentID))
                .build();
    }
}