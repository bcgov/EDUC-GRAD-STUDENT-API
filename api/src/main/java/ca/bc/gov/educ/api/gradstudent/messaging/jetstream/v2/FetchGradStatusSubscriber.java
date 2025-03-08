package ca.bc.gov.educ.api.gradstudent.messaging.jetstream.v2;

import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.v2.GraduationStudentGradStatusRequest;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GraduationStudentRecordGradStatus;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.v2.GraduationStudentGradStatusResponse;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
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
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;

@Component("fetchGradStatusSubscriberv2")
public class FetchGradStatusSubscriber implements MessageHandler {

    private final Connection natsConnection;
    private Dispatcher dispatcher;
    private final GraduationStatusService graduationStatusService;

    private static final String TOPIC = Topics.GRAD_STUDENT_API_FETCH_GRAD_STATUS_TOPIC_V2.toString();

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
            GraduationStudentGradStatusRequest graduationStudentGradStatusRequest = getGraduationStudentGradStatusRequest(event);
            GraduationStudentRecordGradStatus graduationStatus = graduationStatusService.getGraduationStatusProjection(graduationStudentGradStatusRequest.getStudentID());
            boolean isGraduated = isGraduated(graduationStatus, graduationStudentGradStatusRequest.getDate());
            response = getResponse(graduationStudentGradStatusRequest.getStudentID(), isGraduated);
        } catch (Exception e) {
            response = getErrorResponse(e);
            if(!(e instanceof EntityNotFoundException)){
                log.error(String.format("NATS message exception at FetchGradStatusSubscriber: %s when processing: %s", e.getMessage(), eventString));
            }
        }
        this.natsConnection.publish(message.getReplyTo(), response.getBytes());
    }

    private GraduationStudentGradStatusRequest getGraduationStudentGradStatusRequest(Event event) throws JsonProcessingException {
        GraduationStudentGradStatusRequest graduationStudentGradStatusRequest = JsonUtil.getJsonObjectFromString(GraduationStudentGradStatusRequest.class, event.getEventPayload());
        if(graduationStudentGradStatusRequest != null) {
            if(graduationStudentGradStatusRequest.getStudentID() == null || graduationStudentGradStatusRequest.getDate() == null) {
                 throw new IllegalArgumentException("Invalid Input");
           }
        }
        return graduationStudentGradStatusRequest;
    }

    private boolean isGraduated(GraduationStudentRecordGradStatus graduationStatus, LocalDate date) {
        if(graduationStatus != null) {
            if("SCCP".equalsIgnoreCase(graduationStatus.getProgram())) {
                return false;
            } else {
                if(graduationStatus.getProgramCompletionDate() != null) {
                    LocalDate programCompletionDate = graduationStatus.getProgramCompletionDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    return programCompletionDate.isBefore(date) || programCompletionDate.isEqual(date);
                }
            }
        }
        return false;
    }

    private String getResponse(UUID studentID, boolean isGraduated) throws JsonProcessingException {
        GraduationStudentGradStatusResponse gradStatusPayload = GraduationStudentGradStatusResponse.builder()
                .studentID(studentID)
                .isGraduated(isGraduated)
                .build();
        return JsonUtil.getJsonStringFromObject(gradStatusPayload);
    }

    private String getErrorResponse(Exception e) {
        String ex = (e instanceof EntityNotFoundException) ? "not found" : "error";
        GraduationStudentGradStatusResponse gradStatusPayload = GraduationStudentGradStatusResponse.builder()
                .exception(ex)
                .build();
        try {
            return JsonUtil.getJsonStringFromObject(gradStatusPayload);
        } catch (JsonProcessingException exc) {
            return "{\"isGraduated\":  \"\", \"exception\": \"JSON Parsing exception\"}";
        }
    }

    public String getTopic() {
        return TOPIC;
    }

}
