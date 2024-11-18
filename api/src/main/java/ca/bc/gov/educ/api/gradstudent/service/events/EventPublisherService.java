package ca.bc.gov.educ.api.gradstudent.service.events;

import ca.bc.gov.educ.api.gradstudent.messaging.MessagePublisher;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
public class EventPublisherService {

  /**
   * The constant RESPONDING_BACK_TO_NATS_ON_CHANNEL.
   */
  public static final String RESPONDING_BACK_TO_NATS_ON_CHANNEL = "responding back to NATS on {} channel ";

  @Getter(PRIVATE)
  private final MessagePublisher messagePublisher;

  @Autowired
  public EventPublisherService(final MessagePublisher messagePublisher) {
    this.messagePublisher = messagePublisher;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void send(final Event event) throws JsonProcessingException {
    if (event.getReplyTo() != null) {
      log.debug(RESPONDING_BACK_TO_NATS_ON_CHANNEL, event.getReplyTo());
      this.getMessagePublisher().dispatchMessage(event.getReplyTo(), this.eventProcessed(event));
    }
  }

  private byte[] eventProcessed(final Event gradStudentEvent) throws JsonProcessingException {
    final Event event = Event.builder()
        .sagaId(gradStudentEvent.getSagaId())
        .eventType(gradStudentEvent.getEventType())
        .eventOutcome(gradStudentEvent.getEventOutcome())
        .eventPayload(gradStudentEvent.getEventPayload()).build();
    return JsonUtil.getJsonStringFromObject(event).getBytes();
  }

}
