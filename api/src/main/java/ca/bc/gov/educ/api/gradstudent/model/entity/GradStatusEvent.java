package ca.bc.gov.educ.api.gradstudent.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type GradStatus event.
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "STATUS_EVENT")
@Data
@DynamicUpdate
public class GradStatusEvent {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "EVENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    private UUID eventId;

    @NotNull(message = "eventPayload cannot be null")
    @Lob
    @Column(name = "EVENT_PAYLOAD")
    private byte[] eventPayloadBytes;

    @NotNull(message = "eventStatus cannot be null")
    @Column(name = "EVENT_STATUS")
    private String eventStatus;
    @NotNull(message = "eventType cannot be null")
    @Column(name = "EVENT_TYPE")
    private String eventType;
    /**
     * The Create user.
     */
    @Column(name = "CREATE_USER", updatable = false)
    String createUser;
    /**
     * The Create date.
     */
    @Column(name = "CREATE_DATE", updatable = false)
    @PastOrPresent
    LocalDateTime createDate;
    /**
     * The Update user.
     */
    @Column(name = "UPDATE_USER")
    String updateUser;
    /**
     * The Update date.
     */
    @Column(name = "UPDATE_DATE")
    @PastOrPresent
    LocalDateTime updateDate;
    @Column(name = "SAGA_ID", updatable = false)
    private UUID sagaId;
    @NotNull(message = "eventOutcome cannot be null.")
    @Column(name = "EVENT_OUTCOME")
    private String eventOutcome;
    @Column(name = "REPLY_CHANNEL")
    private String replyChannel;

    /**
     * Gets event payload.
     *
     * @return the event payload
     */
    public String getEventPayload() {
        return new String(getEventPayloadBytes(), StandardCharsets.UTF_8);
    }

    /**
     * Sets event payload.
     *
     * @param eventPayload the event payload
     */
    public void setEventPayload(String eventPayload) {
        setEventPayloadBytes(eventPayload.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * The type Student event builder.
     */
    public static class GradStatusEventBuilder {
        /**
         * The Event payload bytes.
         */
        byte[] eventPayloadBytes;

        /**
         * Event payload grad status event . GradStatus event builder.
         *
         * @param eventPayload the event payload
         * @return the student event . student event builder
         */
        public GradStatusEventBuilder eventPayload(String eventPayload) {
            this.eventPayloadBytes = eventPayload.getBytes(StandardCharsets.UTF_8);
            return this;
        }
    }
}