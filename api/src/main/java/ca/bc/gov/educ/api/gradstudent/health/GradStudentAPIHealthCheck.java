package ca.bc.gov.educ.api.gradstudent.health;

import ca.bc.gov.educ.api.gradstudent.messaging.NatsConnection;
import io.nats.client.Connection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GradStudentAPIHealthCheck implements HealthIndicator {

    private final NatsConnection natsConnection;

    public GradStudentAPIHealthCheck(NatsConnection natsConnection) {
        this.natsConnection = natsConnection;
    }

    @Override
    public Health health() {
        return this.healthCheck();
    }

    private Health healthCheck() {
        final Connection connection = this.natsConnection.connection();
        final Connection.Status status = connection != null ? connection.getStatus() : null;
        if (status == null || status == Connection.Status.CLOSED) {
            log.warn("Health Check failed for NATS. Current status: {}", status);
            return Health.down().withDetail("natsStatus", status).build();
        }
        return Health.up().withDetail("natsStatus", status).build();
    }

}
