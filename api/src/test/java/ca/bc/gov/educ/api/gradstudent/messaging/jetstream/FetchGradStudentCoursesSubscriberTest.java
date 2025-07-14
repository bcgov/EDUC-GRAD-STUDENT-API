package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.service.StudentCourseService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FetchGradStudentCoursesSubscriberTest extends BaseIntegrationTest {

    @Mock
    private Message mockMessage;

    @InjectMocks
    FetchGradStudentCoursesSubscriber fetchCoursesSubscriber;

    @Autowired
    EducGradStudentApiConstants constants;
    @Autowired
    StudentCourseService studentCourseService;
    @MockBean
    NatsConnection natsConnection;
    @MockBean
    Connection connection;
    @MockBean
    Dispatcher dispatcher;

    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        openMocks(this);
        when(natsConnection.connection()).thenReturn(connection);

        Field dispatchF = FetchGradStudentCoursesSubscriber.class.getDeclaredField("natsConnection");
        dispatchF.setAccessible(true);
        dispatchF.set(fetchCoursesSubscriber, natsConnection.connection());
    }

    @Test
    public void test_OnMessage_CoursesFound() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = prepareEventMessage(studentID);
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());

        List<StudentCourse> courses = List.of(new StudentCourse(), new StudentCourse());
        when(studentCourseService.getStudentCourses(studentID)).thenReturn(courses);

        assertDoesNotThrow(() -> fetchCoursesSubscriber.onMessage(mockMessage));
    }

    @Test
    public void test_OnMessage_NoCourses() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = prepareEventMessage(studentID);
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());

        when(studentCourseService.getStudentCourses(studentID))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> fetchCoursesSubscriber.onMessage(mockMessage));
    }

    @Test
    public void test_OnMessage_EntityNotFound() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = prepareEventMessage(studentID);
        when(mockMessage.getData()).thenReturn(JsonUtil.getJsonStringFromObject(event).getBytes());

        when(studentCourseService.getStudentCourses(studentID))
                .thenThrow(new ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException());

        assertDoesNotThrow(() -> fetchCoursesSubscriber.onMessage(mockMessage));
    }

    @Test
    public void test_GetTopicName() {
        assertThat(fetchCoursesSubscriber.getTopic())
                .isEqualTo(ca.bc.gov.educ.api.gradstudent.constant.Topics.GRAD_STUDENT_API_FETCH_GRAD_STUDENT_COURSES_TOPIC.toString());
    }

    private Event prepareEventMessage(UUID studentID) throws JsonProcessingException {
        return Event.builder()
                .eventPayload(studentID.toString())
                .build();
    }
}
