package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.service.StudentCourseService;
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FetchGradStudentCoursesSubscriberTest extends BaseIntegrationTest {

    @Mock
    private Message mockNatsMessage;

    @InjectMocks
    private FetchGradStudentCoursesSubscriber fetchGradStudentCoursesSubscriber;

    @MockBean
    private StudentCourseService studentCourseService;

    @MockBean
    private Connection natsConnection;
    @MockBean
    private NatsConnection natsConnectionWrapper;

    @Before
    public void setUp() throws Exception {
        openMocks(this);
        when(natsConnectionWrapper.connection()).thenReturn(natsConnection);

        Field studentCourseServiceField = FetchGradStudentCoursesSubscriber.class.getDeclaredField("gradStudentCourseService");
        studentCourseServiceField.setAccessible(true);
        studentCourseServiceField.set(fetchGradStudentCoursesSubscriber, studentCourseService);

        Field natsConnectionField = FetchGradStudentCoursesSubscriber.class.getDeclaredField("natsConnection");
        natsConnectionField.setAccessible(true);
        natsConnectionField.set(fetchGradStudentCoursesSubscriber, natsConnectionWrapper.connection());
    }

    @Test
    public void testOnMessage_WhenStudentCoursesFound_PublishesResponse() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        byte[] eventPayload = JsonUtil.getJsonBytesFromObject(event);
        when(mockNatsMessage.getData()).thenReturn(eventPayload);

        List<StudentCourse> mockCourses = List.of(new StudentCourse(), new StudentCourse());
        when(studentCourseService.getStudentCourses(studentID)).thenReturn(mockCourses);

        assertDoesNotThrow(() -> fetchGradStudentCoursesSubscriber.onMessage(mockNatsMessage));
    }

    @Test
    public void testOnMessage_WhenStudentNotFound_PublishesErrorResponse() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        byte[] eventPayload = JsonUtil.getJsonBytesFromObject(event);
        when(mockNatsMessage.getData()).thenReturn(eventPayload);

        when(studentCourseService.getStudentCourses(studentID)).thenThrow(new EntityNotFoundException());

        assertDoesNotThrow(() -> fetchGradStudentCoursesSubscriber.onMessage(mockNatsMessage));
    }

    @Test
    public void testOnMessage_WhenNoCoursesFound_PublishesEmptyList() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        byte[] eventPayload = JsonUtil.getJsonBytesFromObject(event);
        when(mockNatsMessage.getData()).thenReturn(eventPayload);

        when(studentCourseService.getStudentCourses(studentID)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> fetchGradStudentCoursesSubscriber.onMessage(mockNatsMessage));
    }
}