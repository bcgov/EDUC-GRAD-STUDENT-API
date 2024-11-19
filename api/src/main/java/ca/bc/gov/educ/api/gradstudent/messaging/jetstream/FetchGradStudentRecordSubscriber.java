package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.GradStatusPayload;
import ca.bc.gov.educ.api.gradstudent.model.dc.GradStudentRecordPayload;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GradStudentRecord;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.nats.client.*;
import lombok.val;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class FetchGradStudentRecordSubscriber implements MessageHandler {

    private final Connection natsConnection;
    private Dispatcher dispatcher;
    private final GradStudentService gradStudentService;
    public static final String RESPONDING_BACK_TO_NATS_ON_CHANNEL = "responding back to NATS on {} channel ";
    public static final String PAYLOAD_LOG = "payload is :: {}";



    private static final String TOPIC = Topics.GRAD_STUDENT_API_FETCH_GRAD_STUDENT_TOPIC.toString();
    private static final Logger log = LoggerFactory.getLogger(FetchGradStudentRecordSubscriber.class);

    @Autowired
    public FetchGradStudentRecordSubscriber(final Connection natsConnection, GradStudentService gradStudentService, EducGradStudentApiConstants constants) {
        this.natsConnection = natsConnection;
        this.gradStudentService = gradStudentService;
    }

    @PostConstruct
    public void subscribe() {
        this.dispatcher = this.natsConnection.createDispatcher(this);
        this.dispatcher.subscribe(TOPIC);
    }

    @Override
    public void onMessage(Message message) {
        val eventString = new String(message.getData());
        log.debug("Received message: {}", eventString);
        String response;

        try {
            Event event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
            log.info("received GET_STUDENT event :: {}", event.getSagaId());
            log.trace(PAYLOAD_LOG, event.getEventPayload());
            UUID studentId = JsonUtil.getJsonObjectFromString(UUID.class, event.getEventPayload());
            GradStudentRecord studentRecord = gradStudentService.getGraduationStudentRecord(studentId);
            response = getResponse(studentRecord);
            log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, message.getReplyTo() != null ? message.getReplyTo() : event.getReplyTo());
            this.natsConnection.publish(message.getReplyTo(), response.getBytes());
        } catch (Exception e) {
            log.error("Error while processing GET_STUDENT event", e);
        }
    }

    private String getResponse(GradStudentRecord studentRecord) throws JsonProcessingException {
        GradStudentRecordPayload gradStudentRecordPayload = GradStudentRecordPayload.builder()
                .dob(studentRecord.getDob() != null ?
                EducGradStudentApiUtils.formatDate(studentRecord.getDob()) : null)
                .build();
        return JsonUtil.getJsonStringFromObject(gradStudentRecordPayload);
    }

    private String getErrorResponse(Exception e) {
        String ex = (e instanceof EntityNotFoundException) ? "not found" : "error";
        GradStatusPayload gradStatusPayload = GradStatusPayload.builder()
                .exception(ex)
                .build();
        try {
            return JsonUtil.getJsonStringFromObject(gradStatusPayload);
        } catch (JsonProcessingException exc) {
            log.error("Error while serializing error response", exc);
            return "{\"dob\": \"\", \"exception\": \"JSON Parsing exception\"}";
        }
    }
}
