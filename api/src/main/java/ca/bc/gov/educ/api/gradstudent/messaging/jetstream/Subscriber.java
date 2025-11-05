package ca.bc.gov.educ.api.gradstudent.messaging.jetstream;

import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.constant.Topics;
import ca.bc.gov.educ.api.gradstudent.exception.IgnoreEventException;
import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.service.JetStreamEventHandlerService;
import ca.bc.gov.educ.api.gradstudent.service.event.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.gradstudent.service.event.PenServicesEventHandlerDelegatorService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EventUtils;
import ca.bc.gov.educ.api.gradstudent.util.LogHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static ca.bc.gov.educ.api.gradstudent.constant.Topics.GRAD_STATUS_EVENT_TOPIC;

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
  private final Map<String, List<String>> streamTopicsMap = new HashMap<>();
  private final EventHandlerDelegatorService eventHandlerDelegatorServiceV1;
  private final PenServicesEventHandlerDelegatorService penServicesEventHandlerDelegatorService;
  private final Executor subscriberExecutor = new EnhancedQueueExecutor.Builder()
          .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("jet-stream-subscriber-%d").build())
          .setCorePoolSize(2).setMaximumPoolSize(2).setKeepAliveTime(Duration.ofMillis(1000)).build();

  /**
   * Instantiates a new Subscriber.
   *
   * @param natsConnection          the nats connection
   * @param jetStreamEventHandlerService the stan event handler service
   */
  @Autowired
  public Subscriber(final Connection natsConnection, final JetStreamEventHandlerService jetStreamEventHandlerService, final EducGradStudentApiConstants constants, EventHandlerDelegatorService eventHandlerDelegatorServiceV1,
                    PenServicesEventHandlerDelegatorService penServicesEventHandlerDelegatorService) {
    this.jetStreamEventHandlerService = jetStreamEventHandlerService;
    this.natsConnection = natsConnection;
    this.constants = constants;
    this.eventHandlerDelegatorServiceV1 = eventHandlerDelegatorServiceV1;
    this.penServicesEventHandlerDelegatorService = penServicesEventHandlerDelegatorService;
    this.initializeStreamTopicMap();
  }

  private void initializeStreamTopicMap() {
    final List<String> gradEventsTopics = new ArrayList<>();
    gradEventsTopics.add(GRAD_STATUS_EVENT_TOPIC.toString());
    final List<String> instituteEventsTopics = new ArrayList<>();
    instituteEventsTopics.add(Topics.STUDENT_ASSESSMENT_EVENTS_TOPIC.name());
    final List<String> studentEventsTopics = new ArrayList<>();
    studentEventsTopics.add(Topics.STUDENT_EVENTS_TOPIC.name());
    final List<String> penServicesEventsTopics = new ArrayList<>();
    penServicesEventsTopics.add(Topics.PEN_SERVICES_EVENTS_TOPIC.name());
    this.streamTopicsMap.put(EducGradStudentApiConstants.STREAM_NAME, gradEventsTopics);
    this.streamTopicsMap.put("ASSESSMENT_EVENTS", instituteEventsTopics);
    this.streamTopicsMap.put("STUDENT_EVENTS", studentEventsTopics);
    this.streamTopicsMap.put("PEN_SERVICES_EVENTS", penServicesEventsTopics);
  }


  /**
   * This subscription will make sure the messages are required to acknowledge manually to Jet Stream.
   * Subscribe.
   *
   * @throws IOException the io exception
   */
  @PostConstruct
  public void subscribe() throws IOException, JetStreamApiException {
    val qName = EducGradStudentApiConstants.API_NAME.concat("-QUEUE");
    val autoAck = false;
    for (val entry : this.streamTopicsMap.entrySet()) {
      for (val topic : entry.getValue()) {
        final PushSubscribeOptions options = PushSubscribeOptions.builder().stream(entry.getKey())
                .durable(EducGradStudentApiConstants.API_NAME.concat("-DURABLE"))
                .configuration(ConsumerConfiguration.builder().deliverPolicy(DeliverPolicy.New).build()).build();
        this.natsConnection.jetStream().subscribe(topic, qName, this.natsConnection.createDispatcher(), this::onMessage,
                autoAck, options);
      }
    }
  }

  /**
   * This method will process the event message pushed into the grad_status_events_topic.
   * this will get the message and update the event status to mark that the event reached the message broker.
   * On message message handler.
   *
   * @param message the string representation of {@link ChoreographedEvent} if it not type of event then it will throw exception and will be ignored.
   */
  public void onMessage(final Message message) {
    log.debug("Received message Subject:: {} , SID :: {} , sequence :: {}, pending :: {} ", message.getSubject(), message.getSID(), message.metaData().consumerSequence(), message.metaData().pendingCount());
    try {
      val eventString = new String(message.getData());
      LogHelper.logMessagingEventDetails(eventString, constants.isSplunkLogHelperEnabled());
      ChoreographedEvent event = EventUtils.getChoreographedEventIfValid(eventString);
      this.subscriberExecutor.execute(() -> {
        try {
          if(event.getEventType().equals(EventType.ASSESSMENT_STUDENT_UPDATE)) {
            this.eventHandlerDelegatorServiceV1.handleChoreographyEvent(event, message);
          } else if(event.getEventType().equals(EventType.UPDATE_STUDENT)) {
            this.eventHandlerDelegatorServiceV1.handleChoreographyEvent(event, message);
          } else if (event.getEventType().equals(EventType.CREATE_MERGE) || event.getEventType().equals(EventType.DELETE_MERGE)) {
            this.penServicesEventHandlerDelegatorService.handleChoreographyEvent(event, message);
          } else{
            jetStreamEventHandlerService.updateEventStatus(event);
            log.info("Ignoring event :: {} ", event);
            message.ack();
          }
        } catch (final IOException e) {
          log.error("IOException ", e);
        }
      });
    } catch (final IgnoreEventException ex) {
      log.warn("Ignoring event with type :: {} :: and event outcome :: {}", ex.getEventType(), ex.getEventOutcome());
      message.ack();
    } catch (final Exception ex) {
      log.error("Exception occurred processing incoming message: ", ex);
    }
  }

}
