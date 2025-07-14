package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.service.StudentCourseService;
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

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FetchGradStudentCoursesSubscriberTest {

    @Mock Connection connection;
    @Mock Dispatcher dispatcher;
    @Mock StudentCourseService studentCourseService;
    @Mock EducGradStudentApiConstants constants;

    @InjectMocks
    private FetchGradStudentCoursesSubscriber subscriber;

    @BeforeEach
    void setUp() {
        when(connection.createDispatcher(eq(subscriber))).thenReturn(dispatcher);
        subscriber.subscribe();
    }

    @Test
    void testOnMessage_WhenStudentCoursesFound_DoesNotThrow() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        List<StudentCourse> courses = List.of(new StudentCourse(), new StudentCourse());
        when(studentCourseService.getStudentCourses(studentID)).thenReturn(courses);

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    void testOnMessage_WhenStudentNotFound_DoesNotThrow() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        when(studentCourseService.getStudentCourses(studentID))
                .thenThrow(new EntityNotFoundException());

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    void testOnMessage_WhenNoCoursesFound_DoesNotThrow() throws JsonProcessingException {
        UUID studentID = UUID.randomUUID();
        Event event = Event.builder().eventPayload(studentID.toString()).build();
        Message msg = mock(Message.class);
        when(msg.getData()).thenReturn(JsonUtil.getJsonBytesFromObject(event));

        when(studentCourseService.getStudentCourses(studentID))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }
}
