package ca.bc.gov.educ.api.gradstudent.service.event;


import ca.bc.gov.educ.api.gradstudent.exception.BusinessException;
import ca.bc.gov.educ.api.gradstudent.messaging.MessagePublisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;
import static ca.bc.gov.educ.api.gradstudent.service.event.EventHandlerService.PAYLOAD_LOG;


/**
 * The type Event handler service.
 */
@Service
@Slf4j
@SuppressWarnings({"java:S3864", "java:S3776"})
public class EventHandlerDelegatorService {

  /**
   * The constant RESPONDING_BACK_TO_NATS_ON_CHANNEL.
   */
  public static final String RESPONDING_BACK_TO_NATS_ON_CHANNEL = "responding back to NATS on {} channel ";
  private final MessagePublisher messagePublisher;
  private final EventHandlerService eventHandlerService;
  private final GraduationStatusService graduationStatusService;
  private final Publisher publisher;

  /**
   * Instantiates a new Event handler delegator service.
   *
   * @param messagePublisher    the message publisher
   * @param eventHandlerService the event handler service
   */
  @Autowired
  public EventHandlerDelegatorService(MessagePublisher messagePublisher, EventHandlerService eventHandlerService, GraduationStatusService graduationStatusService, Publisher publisher) {
    this.messagePublisher = messagePublisher;
    this.eventHandlerService = eventHandlerService;
    this.graduationStatusService = graduationStatusService;
    this.publisher = publisher;
  }

  public void handleChoreographyEvent(@NonNull final ChoreographedEvent choreographedEvent, final Message message) throws IOException {
    try {
      final var persistedEvent = this.graduationStatusService.persistEventToDB(choreographedEvent);
      message.ack(); // acknowledge to Jet Stream that api got the message and it is now in DB.
      log.info("acknowledged to Jet Stream...");
      this.eventHandlerService.handleAssessmentUpdatedDataEvent(persistedEvent);
    } catch (final BusinessException businessException) {
      message.ack(); // acknowledge to Jet Stream that api got the message already...
      log.info("acknowledged to Jet Stream...");
    }
  }

  /**
   * Handle event.
   *
   * @param event   the event
   * @param message the message
   */
  public void handleEvent(final Event event, final Message message) {
    byte[] response;
    boolean isSynchronous = message.getReplyTo() != null;
    try {
      switch (event.getEventType()) {
        case PROCESS_STUDENT_DEM_DATA:
          log.info("Received PROCESS_STUDENT_DEM_DATA event :: {}", event);
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          var pairResponse = eventHandlerService.handleProcessStudentDemDataEvent(event);
          log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, message.getReplyTo() != null ? message.getReplyTo() : event.getReplyTo());
          publishToNATS(event, message, isSynchronous, pairResponse.getLeft());
          if(pairResponse.getRight() != null) {
            publishToJetStream(pairResponse.getRight());
          }
          break;
        case PROCESS_STUDENT_COURSE_DATA:
          log.info("Received PROCESS_STUDENT_COURSE_DATA event :: {}", event);
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleProcessStudentCourseDataEvent(event);
          log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, message.getReplyTo() != null ? message.getReplyTo() : event.getReplyTo());
          publishToNATS(event, message, isSynchronous, response);
          break;
        default:
          log.info("silently ignoring other events :: {}", event);
          break;
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

  private void publishToJetStream(final GradStatusEvent event) {
    publisher.dispatchChoreographyEvent(event);
  }
}
