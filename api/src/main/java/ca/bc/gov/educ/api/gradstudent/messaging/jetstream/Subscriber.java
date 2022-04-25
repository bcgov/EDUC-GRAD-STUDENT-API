package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.service.JetStreamEventHandlerService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import ca.bc.gov.educ.api.gradstudent.util.LogHelper;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

import static ca.bc.gov.educ.api.gradstudent.constant.Topics.GRAD_STATUS_EVENTS_TOPIC;

/**
 * The type Subscriber.
 */
@Component
@DependsOn("publisher")
@Slf4j
public class Subscriber {
  private final JetStreamEventHandlerService jetStreamEventHandlerService;
  private final Connection natsConnection;
  private final EducGradStudentApiConstants constants;

  /**
   * Instantiates a new Subscriber.
   *
   * @param natsConnection          the nats connection
   * @param jetStreamEventHandlerService the stan event handler service
   */
  @Autowired
  public Subscriber(final Connection natsConnection, final JetStreamEventHandlerService jetStreamEventHandlerService, final EducGradStudentApiConstants constants) {
    this.jetStreamEventHandlerService = jetStreamEventHandlerService;
    this.natsConnection = natsConnection;
    this.constants = constants;
  }


  /**
   * This subscription will make sure the messages are required to acknowledge manually to Jet Stream.
   * Subscribe.
   *
   * @throws IOException the io exception
   */
  @PostConstruct
  public void subscribe() throws IOException, JetStreamApiException {
    val qName = "GRAD-STATUS-EVENTS-TOPIC-GRAD-STUDENT-API-QUEUE";
    val autoAck = false;
    PushSubscribeOptions options = PushSubscribeOptions.builder().stream(EducGradStudentApiConstants.STREAM_NAME)
        .durable("GRAD-STATUS-EVENTS-TOPIC-GRAD-STUDENT-API-DURABLE")
        .configuration(ConsumerConfiguration.builder().deliverPolicy(DeliverPolicy.New).build()).build();
    this.natsConnection.jetStream().subscribe(GRAD_STATUS_EVENTS_TOPIC.toString(), qName, this.natsConnection.createDispatcher(), this::onGradStatusEventsTopicMessage,
        autoAck, options);
  }

  /**
   * This method will process the event message pushed into the grad_status_events_topic.
   * this will get the message and update the event status to mark that the event reached the message broker.
   * On message message handler.
   *
   * @param message the string representation of {@link ChoreographedEvent} if it not type of event then it will throw exception and will be ignored.
   */
  public void onGradStatusEventsTopicMessage(final Message message) {
    log.info("Received message Subject:: {} , SID :: {} , sequence :: {}, pending :: {} ", message.getSubject(), message.getSID(), message.metaData().consumerSequence(), message.metaData().pendingCount());
    try {
      val eventString = new String(message.getData());
      LogHelper.logMessagingEventDetails(eventString, constants.isSplunkLogHelperEnabled());
      ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
      jetStreamEventHandlerService.updateEventStatus(event);
      log.debug("received event :: {} ", event);
      message.ack();
    } catch (final Exception ex) {
      log.error("Exception ", ex);
    }
  }

}
