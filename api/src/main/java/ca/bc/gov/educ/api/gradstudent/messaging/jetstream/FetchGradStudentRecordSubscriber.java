package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.GradStudentRecordCourses;
import ca.bc.gov.educ.api.gradstudent.model.dc.GradStudentRecordCoursesLoad;
import ca.bc.gov.educ.api.gradstudent.model.dc.GradStudentRecordPayload;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GradStudentRecord;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

@Component
public class FetchGradStudentRecordSubscriber implements MessageHandler {

    private final Connection natsConnection;
    private Dispatcher dispatcher;
    private final GradStudentService gradStudentService;
    private final Executor subscriberExecutor;
    public static final String RESPONDING_BACK_TO_NATS_ON_CHANNEL = "responding back to NATS on {} channel ";
    public static final String PAYLOAD_LOG = "payload is :: {}";
    private static final String TOPIC = Topics.GRAD_STUDENT_API_FETCH_GRAD_STUDENT_TOPIC.toString();
    private static final Logger log = LoggerFactory.getLogger(FetchGradStudentRecordSubscriber.class);

    @Autowired
    public FetchGradStudentRecordSubscriber(final Connection natsConnection, GradStudentService gradStudentService, EducGradStudentApiConstants constants,
                                            @Qualifier("subscriberExecutor") Executor subscriberExecutor) {
        this.natsConnection = natsConnection;
        this.gradStudentService = gradStudentService;
        this.subscriberExecutor = subscriberExecutor;
    }

    @PostConstruct
    public void subscribe() {
        this.dispatcher = this.natsConnection.createDispatcher(this);
        this.dispatcher.subscribe(TOPIC);
    }

    @Override
    public void onMessage(Message message) {
        Runnable task = () -> {
            val eventString = new String(message.getData());
            log.debug("Received message: {}", eventString);
            String response;

            try {
                Event event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
                log.info("received GET_STUDENT event :: {}", event.getSagaId());
                log.trace(PAYLOAD_LOG, event.getEventPayload());
                UUID studentId = UUID.fromString(event.getEventPayload());
                GradStudentRecord studentRecord = gradStudentService.getGraduationStudentRecord(studentId);
                response = getResponse(studentRecord);
                log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, message.getReplyTo() != null ? message.getReplyTo() : event.getReplyTo());
            } catch (Exception e) {
                response = getErrorResponse(e);
                if(!(e instanceof EntityNotFoundException)) {
                    log.error("Error while processing GET_STUDENT event", e);
                }
            }
            this.natsConnection.publish(message.getReplyTo(), response.getBytes());
        };
        if (this.subscriberExecutor != null) {
            this.subscriberExecutor.execute(task);
        } else {
            task.run();
        }
    }

    public String getResponse(GradStudentRecord studentRecord) throws JsonProcessingException {
        var gradStudentCoursePayload = gradStudentService.setGradMetaData(studentRecord.getStudentGradData());
        GradStudentRecordPayload gradStudentRecordPayload = GradStudentRecordPayload.builder()
                .studentID(String.valueOf(studentRecord.getStudentID()))
                .program(studentRecord.getProgram())
                .programCompletionDate(studentRecord.getProgramCompletionDate() != null ? EducGradStudentApiUtils.formatDate(studentRecord.getProgramCompletionDate()) : null)
                .schoolOfRecordId(String.valueOf(studentRecord.getSchoolOfRecordId()))
                .studentStatusCode(studentRecord.getStudentStatus())
                .schoolAtGradId(studentRecord.getSchoolAtGradId() != null ? studentRecord.getSchoolAtGradId().toString() : null)
                .graduated(String.valueOf(gradStudentCoursePayload != null && gradStudentCoursePayload.isGraduated()))
                .courseList(gradStudentCoursePayload != null && gradStudentCoursePayload.getStudentCourses() != null
                                ? translateStudentCourseList(gradStudentCoursePayload.getStudentCourses().getStudentCourseList())
                                : null)
                .studentGrade(studentRecord.getStudentGrade())
                .build();
        return JsonUtil.getJsonStringFromObject(gradStudentRecordPayload);
    }
    
    private List<GradStudentRecordCourses> translateStudentCourseList(List<GradStudentRecordCoursesLoad> studentCourseList) {
        var newCourseList = new ArrayList<GradStudentRecordCourses>();
        
        studentCourseList.forEach(student -> {
           GradStudentRecordCourses newCourse = new GradStudentRecordCourses();
           newCourse.setCourseCode(student.getCourseCode());
           newCourse.setCourseLevel(student.getCourseLevel());
           newCourse.setCourseSession(student.getSessionDate());
           newCourse.setGradReqMet(StringUtils.isNotBlank(student.getGradReqMet()) ? student.getGradReqMet() : null);

           newCourseList.add(newCourse);
        });
        return newCourseList;
    }

    private String getErrorResponse(Exception e) {
        String ex = (e instanceof EntityNotFoundException) ? "not found" : "error";
        GradStudentRecordPayload gradStudentRecordPayload = GradStudentRecordPayload.builder()
                .exception(ex)
                .build();
        try {
            return JsonUtil.getJsonStringFromObject(gradStudentRecordPayload);
        } catch (JsonProcessingException exc) {
            log.error("Error while serializing error response", exc);
            return "{\"studentID\": \"\", \"program\": \"\", \"programCompletionDate\": \"\", \"schoolOfRecord\": \"\", \"studentStatusCode\": \"\", \"graduated\": \"\", \"exception\": \"JSON Parsing exception\"}";
        }
    }

    public String getTopic() {
        return TOPIC;
    }
}
