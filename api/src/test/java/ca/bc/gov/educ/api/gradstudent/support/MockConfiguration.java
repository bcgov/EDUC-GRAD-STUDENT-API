package ca.bc.gov.educ.api.gradstudent.support;

import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStudentRecordSubscriber;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Subscriber;
import io.nats.client.Connection;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * The type Mock configuration.
 */
@Profile("integration-test")
@Configuration
public class MockConfiguration {

  @Bean
  @Primary
  public RestTemplate restTemplate() {
    return Mockito.mock(RestTemplate.class);
  }

  @Bean
  @Primary
  public WebClient webClient() {
    return Mockito.mock(WebClient.class);
  }

  @Bean
  @Primary
  public NatsConnection natsConnection() {
    return Mockito.mock(NatsConnection.class);
  }

  @Bean
  @Primary
  public Publisher publisher() {
    return Mockito.mock(Publisher.class);
  }

  @Bean
  @Primary
  public Subscriber subscriber() {
    return Mockito.mock(Subscriber.class);
  }

  @Bean
  @Primary
  public ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber fetchGradStatusSubscriber() {
    return Mockito.mock(ca.bc.gov.educ.api.gradstudent.messaging.jetstream.FetchGradStatusSubscriber.class);
  }

  @Bean
  @Primary
  public ca.bc.gov.educ.api.gradstudent.messaging.jetstream.v2.FetchGradStatusSubscriber fetchGradStatusSubscriberv2() {
    return Mockito.mock(ca.bc.gov.educ.api.gradstudent.messaging.jetstream.v2.FetchGradStatusSubscriber.class);
  }

  @Bean
  @Primary
  public FetchGradStudentRecordSubscriber fetchGradStudentRecordSubscriber() {return Mockito.mock(FetchGradStudentRecordSubscriber.class);
  }

  @Bean
  @Primary
  public Connection connection() {
    return Mockito.mock(Connection.class);
  }

}
