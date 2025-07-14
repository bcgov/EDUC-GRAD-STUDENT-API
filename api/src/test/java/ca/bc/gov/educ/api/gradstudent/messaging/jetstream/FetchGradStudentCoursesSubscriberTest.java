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
import io.nats.client.MessageHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FetchGradStudentCoursesSubscriberTest {

    @Mock Connection connection;
    @Mock Dispatcher dispatcher;
    @Mock StudentCourseService studentCourseService;
    @Mock EducGradStudentApiConstants constants;

    @InjectMocks
    private FetchGradStudentCoursesSubscriber subscriber;

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
    void whenCoursesExist_thenNoException() throws Exception {
        UUID id = UUID.randomUUID();
        Message msg = buildMsg(id);

        List<StudentCourse> list = List.of(new StudentCourse(), new StudentCourse());
        when(studentCourseService.getStudentCourses(id)).thenReturn(list);

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    void whenNoCourses_thenNoException() throws Exception {
        UUID id = UUID.randomUUID();
        Message msg = buildMsg(id);
        when(studentCourseService.getStudentCourses(id))
                .thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }

    @Test
    void whenNotFound_thenNoException() throws Exception {
        UUID id = UUID.randomUUID();
        Message msg = buildMsg(id);
        when(studentCourseService.getStudentCourses(id))
                .thenThrow(new EntityNotFoundException());

        assertDoesNotThrow(() -> subscriber.onMessage(msg));
    }
}
