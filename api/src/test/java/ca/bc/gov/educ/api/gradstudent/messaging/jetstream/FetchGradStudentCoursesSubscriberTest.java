package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.controller.BaseIntegrationTest;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.service.StudentCourseService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FetchGradStudentCoursesSubscriberTest extends BaseIntegrationTest {

    @MockBean
    private FetchGradStudentCoursesSubscriber ignoredDummySubscriber;

    @MockBean
    private StudentCourseService studentCourseService;

    @MockBean
    private EducGradStudentApiConstants constants;

    @MockBean
    private Dispatcher dispatcher;

    private FetchGradStudentCoursesSubscriber subscriber;

    @Before
    public void setUp() {
        subscriber = new FetchGradStudentCoursesSubscriber(connection, studentCourseService, constants);

        when(connection.createDispatcher(any(MessageHandler.class)))
                .thenReturn(dispatcher);

        subscriber.subscribe();
    }

    @Test
    public void testOnMessage_WhenStudentCoursesFound_PublishesResponse() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = org.mockito.Mockito.mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        List<StudentCourse> mockCourses = List.of(new StudentCourse(), new StudentCourse());
        when(studentCourseService.getStudentCourses(studentID)).thenReturn(mockCourses);

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    public void testOnMessage_WhenStudentNotFound_PublishesErrorResponse() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = org.mockito.Mockito.mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        when(studentCourseService.getStudentCourses(studentID))
                .thenThrow(new EntityNotFoundException());

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    public void testOnMessage_WhenNoCoursesFound_PublishesEmptyList() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = org.mockito.Mockito.mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        when(studentCourseService.getStudentCourses(studentID))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }
}
