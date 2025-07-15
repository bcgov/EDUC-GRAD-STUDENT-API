package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.service.StudentCourseService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.Connection;
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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FetchGradStudentCoursesSubscriberTest extends BaseIntegrationTest {

    @Mock
    private Message mockMessage;

    @InjectMocks
    private FetchGradStudentCoursesSubscriber fetchCoursesSubscriber;

    @MockBean
    private StudentCourseService studentCourseService;

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

        Field studentCourseServiceField = FetchGradStudentCoursesSubscriber.class.getDeclaredField("gradStudentCourseService");
        studentCourseServiceField.setAccessible(true);
        studentCourseServiceField.set(fetchCoursesSubscriber, studentCourseService);

        Field natsConnectionField = FetchGradStudentCoursesSubscriber.class.getDeclaredField("natsConnection");
        natsConnectionField.setAccessible(true);
        natsConnectionField.set(fetchCoursesSubscriber, connection);
    }

    @Test
    public void testOnMessage_CoursesFound() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = prepareEventMessage(studentID);
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());
        when(mockMessage.getReplyTo()).thenReturn("replyTo");

        List<StudentCourse> courses = List.of(new StudentCourse(), new StudentCourse());
        when(studentCourseService.getStudentCourses(studentID)).thenReturn(courses);

        assertDoesNotThrow(() -> fetchCoursesSubscriber.onMessage(mockMessage));
    }

    @Test
    public void testOnMessage_NoCourses() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = prepareEventMessage(studentID);
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());
        when(mockMessage.getReplyTo()).thenReturn("replyTo");

        when(studentCourseService.getStudentCourses(studentID))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> fetchCoursesSubscriber.onMessage(mockMessage));
    }

    @Test
    public void testOnMessage_EntityNotFound() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = prepareEventMessage(studentID);
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());
        when(mockMessage.getReplyTo()).thenReturn("replyTo");

        when(studentCourseService.getStudentCourses(studentID))
                .thenThrow(new EntityNotFoundException());

        assertDoesNotThrow(() -> fetchCoursesSubscriber.onMessage(mockMessage));
    }

    @Test
    public void testGetTopicName() {
        assertThat(fetchCoursesSubscriber.getTopic())
                .isEqualTo(Topics.GRAD_STUDENT_API_FETCH_GRAD_STUDENT_COURSES_TOPIC.toString());
    }

    private Event prepareEventMessage(UUID studentID) {
        Event event = new Event();
        event.setEventPayload(studentID.toString());
        return event;
    }
}