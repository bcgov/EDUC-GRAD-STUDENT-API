package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.GradStudentCourseRecordsPayload;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.service.StudentCourseService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

@Component
public class FetchGradStudentCoursesSubscriber implements MessageHandler {

    private final Connection natsConnection;
    private Dispatcher dispatcher;
    private final StudentCourseService gradStudentCourseService;
    private final Executor subscriberExecutor;
    public static final String RESPONDING_BACK_TO_NATS_ON_CHANNEL = "responding back to NATS on {} channel ";
    public static final String PAYLOAD_LOG = "payload is :: {}";
    private static final String TOPIC = Topics.GRAD_STUDENT_API_FETCH_GRAD_STUDENT_COURSES_TOPIC.toString();
    private static final Logger log = LoggerFactory.getLogger(FetchGradStudentCoursesSubscriber.class);

    @Autowired
    public FetchGradStudentCoursesSubscriber(final Connection natsConnection, StudentCourseService gradStudentCourseService, EducGradStudentApiConstants constants,
                                             @Qualifier("subscriberExecutor") Executor subscriberExecutor) {
        this.natsConnection = natsConnection;
        this.gradStudentCourseService = gradStudentCourseService;
        this.subscriberExecutor = subscriberExecutor;
    }

    @PostConstruct
    public void subscribe() {
        this.dispatcher = this.natsConnection.createDispatcher(this);
        this.dispatcher.subscribe(TOPIC);
    }

    @Override
    public void onMessage(Message message) {
        this.subscriberExecutor.execute(() -> {
            val eventString = new String(message.getData());
            log.debug("Received message: {}", eventString);
            String response;

            try {
                Event event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
                log.debug("received GET_STUDENT_COURSES event :: {}", event.getSagaId());
                log.trace(PAYLOAD_LOG, event.getEventPayload());
                UUID studentId = UUID.fromString(event.getEventPayload());
                List<StudentCourse> studentCourseRecords = gradStudentCourseService.getStudentCourses(studentId);
                response = getResponse(studentCourseRecords);
                log.debug(RESPONDING_BACK_TO_NATS_ON_CHANNEL, message.getReplyTo() != null ? message.getReplyTo() : event.getReplyTo());
            } catch (Exception e) {
                response = getErrorResponse(e);
                log.error("Error while processing GET_STUDENT_COURSES event", e);
            }
            this.natsConnection.publish(message.getReplyTo(), response.getBytes());
        });
    }

    private String getResponse(List<StudentCourse> studentCourseRecords) throws JsonProcessingException {
        GradStudentCourseRecordsPayload gradStudentRecordPayload = GradStudentCourseRecordsPayload.builder()
                .courses(studentCourseRecords)
                .build();
        return JsonUtil.getJsonStringFromObject(gradStudentRecordPayload);
    }

    private String getErrorResponse(Exception e) {
        String ex = (e instanceof EntityNotFoundException) ? "not found" : "error";
        GradStudentCourseRecordsPayload gradStudentCoursesRecordPayload = GradStudentCourseRecordsPayload.builder()
                .exception(ex)
                .build();
        try {
            return JsonUtil.getJsonStringFromObject(gradStudentCoursesRecordPayload);
        } catch (JsonProcessingException exc) {
            log.error("Error while serializing error response", exc);
            return "{\"exception\": \"JSON Parsing exception\"}";
        }
    }

    public String getTopic() {
        return TOPIC;
    }
}
