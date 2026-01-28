package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.GradStatusPayload;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GraduationStudentRecordGradStatus;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
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
import java.util.UUID;
import java.util.concurrent.Executor;

@Component
public class FetchGradStatusSubscriber implements MessageHandler {

    private final Connection natsConnection;
    private Dispatcher dispatcher;
    private final GraduationStatusService graduationStatusService;
    private final Executor subscriberExecutor;

    private static final String TOPIC = Topics.GRAD_STUDENT_API_FETCH_GRAD_STATUS_TOPIC.toString();

    private static final Logger log = LoggerFactory.getLogger(FetchGradStatusSubscriber.class);

    @Autowired
    public FetchGradStatusSubscriber(final Connection natsConnection, GraduationStatusService graduationStatusService, EducGradStudentApiConstants constants,
                                     @Qualifier("subscriberExecutor") Executor subscriberExecutor) {
        this.natsConnection = natsConnection;
        this.graduationStatusService = graduationStatusService;
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
            log.debug(eventString);
            String response;
            try {
                Event event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
                UUID stdId = JsonUtil.getJsonObjectFromString(UUID.class, event.getEventPayload());
                GraduationStudentRecordGradStatus graduationStatus = graduationStatusService.getGraduationStatusProjection(stdId);
                response = getResponse(graduationStatus);
            } catch (Exception e) {
                response = getErrorResponse(e);
                if(!(e instanceof EntityNotFoundException)){
                    log.error(String.format("NATS message exception at FetchGradStatusSubscriber: %s when processing: %s", e.getMessage(), eventString));
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

    private String getResponse(GraduationStudentRecordGradStatus graduationStudentRecord) throws JsonProcessingException {
        GradStatusPayload gradStatusPayload = GradStatusPayload.builder()
                .program(graduationStudentRecord.getProgram())
                .programCompletionDate(
                        graduationStudentRecord.getProgramCompletionDate() != null ?
                        EducGradStudentApiUtils.formatDate(graduationStudentRecord.getProgramCompletionDate()) : null)
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

    public String getTopic() {
        return TOPIC;
    }

}
