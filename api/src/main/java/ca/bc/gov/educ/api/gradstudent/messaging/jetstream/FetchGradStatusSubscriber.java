package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.GradStatusPayload;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
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
public class FetchGradStatusSubscriber implements MessageHandler {

    private final Connection natsConnection;
    private Dispatcher dispatcher;
    private final GraduationStatusService graduationStatusService;

    private static final String TOPIC = Topics.GRAD_STUDENT_API_FETCH_GRAD_STATUS_TOPIC.toString();

    private static final Logger log = LoggerFactory.getLogger(FetchGradStatusSubscriber.class);

    @Autowired
    public FetchGradStatusSubscriber(final Connection natsConnection, GraduationStatusService graduationStatusService, EducGradStudentApiConstants constants) {
        this.natsConnection = natsConnection;
        this.graduationStatusService = graduationStatusService;
    }

    @PostConstruct
    public void subscribe() {
        this.dispatcher = this.natsConnection.createDispatcher(this);
        this.dispatcher.subscribe(TOPIC);
    }

    @Override
    public void onMessage(Message message) {
        val eventString = new String(message.getData());
        log.debug(eventString);
        String response;
        try {
            Event event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
            UUID stdId = JsonUtil.getJsonObjectFromString(UUID.class, event.getEventPayload());
            GraduationStudentRecord graduationStatus = graduationStatusService.getGraduationStatus(stdId);
            response = getResponse(graduationStatus);
        } catch (Exception e) {
            response = getErrorResponse(e);
            if(!(e instanceof EntityNotFoundException)){
                log.error(String.format("NATS message exception at FetchGradStatusSubscriber: %s when processing: %s", e.getMessage(), eventString));
            }
        }
        this.natsConnection.publish(message.getReplyTo(), response.getBytes());
    }

    private String getResponse(GraduationStudentRecord graduationStudentRecord) throws JsonProcessingException {
        GradStatusPayload gradStatusPayload = GradStatusPayload.builder()
                .program(graduationStudentRecord.getProgram())
                .programCompletionDate(graduationStudentRecord.getProgramCompletionDate())
                .build();
        return JsonUtil.getJsonStringFromObject(gradStatusPayload);
    }

    private String getErrorResponse(Exception e) {
        String ex = (e instanceof EntityNotFoundException) ? "not found" : "error";
        GradStatusPayload gradStatusPayload = GradStatusPayload.builder()
                .exception(ex)
                .build();
        try {
            return JsonUtil.getJsonStringFromObject(gradStatusPayload);
        } catch (JsonProcessingException exc) {
            return "{\"program\": \"\", \"programCompletionDate\": \"\", \"exception\": \"JSON Parsing exception\"}";
        }
    }

}
