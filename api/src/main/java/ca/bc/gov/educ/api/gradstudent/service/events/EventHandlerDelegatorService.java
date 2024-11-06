package ca.bc.gov.educ.api.gradstudent.service.events;

import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.messaging.MessagePublisher;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import io.nats.client.Message;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
public class EventHandlerDelegatorService {
  public static final String PAYLOAD_LOG = "Payload is :: {}";

  @Getter
  private final EventHandlerService eventHandlerService;

  @Getter(PRIVATE)
  private final EventPublisherService eventPublisherService;
  private final MessagePublisher messagePublisher;

  @Autowired
  public EventHandlerDelegatorService(final EventHandlerService eventHandlerService, final EventPublisherService eventPublisherService, final MessagePublisher messagePublisher) {
    this.eventHandlerService = eventHandlerService;
    this.eventPublisherService = eventPublisherService;
    this.messagePublisher = messagePublisher;
  }

  @Async("subscriberExecutor")
  public void handleEvent(final Event event, final Message message) {
    boolean isSynchronous = message.getReplyTo() != null;
    try {
      log.debug("Received {} from topic event :: {}", event.getEventType(), event.getSagaId());
      log.trace(PAYLOAD_LOG, event.getEventPayload());
      if (event.getEventType() == EventType.ARCHIVE_STUDENTS_REQUEST) {
        var eventResponse = this.getEventHandlerService().handleArchiveStudentsRequest(event);
        publishToNATS(event, message, isSynchronous, eventResponse);
      } else if (event.getEventType() == EventType.ARCHIVE_STUDENTS) {
        var eventResponse = this.getEventHandlerService().archiveStudents(event);
        publishToNATS(event, message, isSynchronous, eventResponse);
      } else {
        log.debug("Silently ignoring other event :: {}", event);
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }

  private void publishToNATS(Event event, Message message, boolean isSynchronous, byte[] left) {
    if (isSynchronous) { // sync, req/reply pattern of nats
      messagePublisher.dispatchMessage(message.getReplyTo(), left);
    } else { // async, pub/sub
      messagePublisher.dispatchMessage(event.getReplyTo(), left);
    }
  }
}
