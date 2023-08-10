package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import io.nats.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.UUID;

@Component
public class FetchGradStatusSubscriber implements MessageHandler {

    private final Connection natsConnection;
    private Dispatcher dispatcher;
    private final GraduationStatusService graduationStatusService;
    private static final String TOPIC = "FETCH_GRAD_STATUS";

    @Autowired
    public FetchGradStatusSubscriber(final Connection natsConnection, GraduationStatusService graduationStatusService) {
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
        String response;
        try {
            UUID stdId = UUID.fromString(new String(message.getData()));
            boolean hasStudentGraduated = graduationStatusService.hasStudentGraduated(stdId);
            response = String.valueOf(hasStudentGraduated);
        } catch (Exception e) {
            response = (e instanceof EntityNotFoundException) ? "not found" : "error";
        }
        this.natsConnection.publish(message.getReplyTo(), response.getBytes());
    }

}
