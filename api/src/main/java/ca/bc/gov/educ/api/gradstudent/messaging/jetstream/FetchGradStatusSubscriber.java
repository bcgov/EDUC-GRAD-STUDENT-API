package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import ca.bc.gov.educ.api.gradstudent.util.LogHelper;
import io.nats.client.*;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class FetchGradStatusSubscriber implements MessageHandler {

    private final Connection natsConnection;
    private Dispatcher dispatcher;
    private final GraduationStatusService graduationStatusService;

    private final EducGradStudentApiConstants constants;
    private static final String TOPIC = "FETCH_GRAD_STATUS";

    @Autowired
    public FetchGradStatusSubscriber(final Connection natsConnection, GraduationStatusService graduationStatusService, EducGradStudentApiConstants constants) {
        this.natsConnection = natsConnection;
        this.graduationStatusService = graduationStatusService;
        this.constants = constants;
    }

    @PostConstruct
    public void subscribe() {
        this.dispatcher = this.natsConnection.createDispatcher(this);
        this.dispatcher.subscribe(TOPIC);
    }

    @Override
    public void onMessage(Message message) {
        val eventString = new String(message.getData());
        LogHelper.logMessagingEventDetails(eventString, constants.isSplunkLogHelperEnabled());
        String response;
        try {
            Event event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
            UUID stdId = UUID.fromString(event.getEventPayload());
            boolean hasStudentGraduated = graduationStatusService.hasStudentGraduated(stdId);
            response = String.valueOf(hasStudentGraduated);
        } catch (Exception e) {
            response = (e instanceof EntityNotFoundException) ? "not found" : "error";
            LogHelper.logMessagingEventDetails("NATS message exception at FetchGradStatusSubscriber: " + response + " when processing: " + eventString, constants.isSplunkLogHelperEnabled());
        }
        this.natsConnection.publish(message.getReplyTo(), response.getBytes());
    }

}
