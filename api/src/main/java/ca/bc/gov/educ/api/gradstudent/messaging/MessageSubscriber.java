package ca.bc.gov.educ.api.gradstudent.messaging;

import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.orchestrator.base.EventHandler;
import ca.bc.gov.educ.api.gradstudent.service.events.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import ca.bc.gov.educ.api.gradstudent.util.LogHelper;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ca.bc.gov.educ.api.gradstudent.constant.Topics.GRAD_STUDENT_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class MessageSubscriber {

  @Getter(PRIVATE)
  private final Map<String, EventHandler> handlerMap = new HashMap<>();
  @Getter(PRIVATE)
  private final EventHandlerDelegatorService eventHandlerDelegatorService;
  private final Connection connection;
  private final EducGradStudentApiConstants constants;

  @Autowired
  public MessageSubscriber(final Connection con, final List<EventHandler> eventHandlers, final EducGradStudentApiConstants constants, final EventHandlerDelegatorService eventHandlerDelegatorService) {
    this.eventHandlerDelegatorService = eventHandlerDelegatorService;
    this.connection = con;
    eventHandlers.forEach(handler -> {
      this.handlerMap.put(handler.getTopicToSubscribe(), handler);

      this.subscribeForSAGA(handler.getTopicToSubscribe(), handler);
    });
    this.constants = constants;
  }

  @PostConstruct
  public void subscribe() {
    final String queue = GRAD_STUDENT_API_TOPIC.toString().replace("_", "-");
    final var dispatcher = this.connection.createDispatcher(this.onMessage());
    dispatcher.subscribe(GRAD_STUDENT_API_TOPIC.toString(), queue);
  }

  public MessageHandler onMessage() {
    return (Message message) -> {
      if (message != null) {
        log.info("Message received subject :: {},  replyTo :: {}, subscriptionID :: {}", message.getSubject(), message.getReplyTo(), message.getSID());
        try {
          final var eventString = new String(message.getData());
          LogHelper.logMessagingEventDetails(eventString, constants.isSplunkLogHelperEnabled());
          final var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          eventHandlerDelegatorService.handleEvent(event, message);
        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }

  private static MessageHandler onMessageForSAGA(final EventHandler eventHandler) {
    return (Message message) -> {
      if (message != null) {
        log.info("Message received subject :: {},  replyTo :: {}, subscriptionID :: {}", message.getSubject(), message.getReplyTo(), message.getSID());
        try {
          final var eventString = new String(message.getData());
          final var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          eventHandler.handleEvent(event);
        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }

  private void subscribeForSAGA(final String topic, final EventHandler eventHandler) {
    this.handlerMap.computeIfAbsent(topic, k -> eventHandler);
    final String queue = topic.replace("_", "-");
    final var dispatcher = this.connection.createDispatcher(MessageSubscriber.onMessageForSAGA(eventHandler));
    dispatcher.subscribe(topic, queue);
  }
}
