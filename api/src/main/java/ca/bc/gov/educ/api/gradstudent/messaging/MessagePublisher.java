package ca.bc.gov.educ.api.gradstudent.messaging;

import io.nats.client.Connection;
import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * The type Message publisher.
 */
@Component
@Slf4j
public class MessagePublisher {


  private final Connection connection;

  @Autowired
  public MessagePublisher(final Connection con) {
    this.connection = con;
  }

  /**
   * Dispatch message.
   *
   * @param subject the subject
   * @param message the message
   */
  public void dispatchMessage(final String subject, final byte[] message) {
    this.connection.publish(subject, message);
  }

  public CompletableFuture<Message> requestMessage(final String subject, final byte[] message) {
    return this.connection.request(subject, message);
  }
}
