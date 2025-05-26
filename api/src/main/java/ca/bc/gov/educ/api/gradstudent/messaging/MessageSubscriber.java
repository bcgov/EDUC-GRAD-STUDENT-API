package ca.bc.gov.educ.api.gradstudent.messaging;

import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.service.event.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import ca.bc.gov.educ.api.gradstudent.util.LogHelper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.Executor;

import static ca.bc.gov.educ.api.gradstudent.constant.Topics.GRAD_STUDENT_API_TOPIC;

@Component
@Slf4j
public class MessageSubscriber {
  private final Executor messageProcessingThreads;
  private final EventHandlerDelegatorService eventHandlerDelegatorServiceV1;
  private final Connection connection;
  private final EducGradStudentApiConstants constants;

  /**
   * Instantiates a new Message subscriber.
   *
   * @param connection                     the nats connection
   * @param eventHandlerDelegatorServiceV1 the event handler delegator service v 1
   */
  @Autowired
  public MessageSubscriber(final Connection connection, EventHandlerDelegatorService eventHandlerDelegatorServiceV1, EducGradStudentApiConstants constants) {
    this.eventHandlerDelegatorServiceV1 = eventHandlerDelegatorServiceV1;
    this.connection = connection;
    this.constants = constants;
    messageProcessingThreads = new EnhancedQueueExecutor.Builder().setThreadFactory(new ThreadFactoryBuilder().setNameFormat("nats-message-subscriber-%d").build()).setCorePoolSize(10).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();
  }

  /**
   * This subscription will makes sure the messages are required to acknowledge manually to STAN.
   * Subscribe.
   */
  @PostConstruct
  public void subscribe() {
    String queue = GRAD_STUDENT_API_TOPIC.toString().replace("_", "-");
    var dispatcher = connection.createDispatcher(onMessage());
    dispatcher.subscribe(GRAD_STUDENT_API_TOPIC.toString(), queue);
  }

  /**
   * On message message handler.
   *
   * @return the message handler
   */
  private MessageHandler onMessage() {
    return (Message message) -> {
      if (message != null) {
        try {
          var eventString = new String(message.getData());
          LogHelper.logMessagingEventDetails(eventString, constants.isSplunkLogHelperEnabled());
          var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          log.debug("message sub handling: {}, {}", event, message);
          messageProcessingThreads.execute(() -> eventHandlerDelegatorServiceV1.handleEvent(event, message));
        } catch (final Exception e) {
          log.debug("on message error: {}", e.getMessage());
          log.error("Exception ", e);
        }
      }
    };
  }


}
