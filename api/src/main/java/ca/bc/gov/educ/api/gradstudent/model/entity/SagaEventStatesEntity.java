package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "GRAD_STUDENT_SAGA_EVENT_STATES")
@DynamicUpdate
public class SagaEventStatesEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
      @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "SAGA_EVENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID sagaEventId;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @ManyToOne
  @JoinColumn(name = "SAGA_ID", updatable = false, columnDefinition = "BINARY(16)")
  SagaEntity saga;

  @NotNull(message = "saga_event_state cannot be null")
  @Column(name = "SAGA_EVENT_STATE")
  String sagaEventState;

  @NotNull(message = "saga_event_outcome cannot be null")
  @Column(name = "SAGA_EVENT_OUTCOME")
  String sagaEventOutcome;

  @NotNull(message = "saga_step_number cannot be null")
  @Column(name = "SAGA_STEP_NUMBER")
  Integer sagaStepNumber;

  @Column(name = "SAGA_EVENT_RESPONSE",  length = 10485760)
  String sagaEventResponse;

  @NotNull(message = "create user cannot be null")
  @Column(name = "CREATE_USER", updatable = false)
  @Size(max = 50)
  String createUser;

  @NotNull(message = "update user cannot be null")
  @Column(name = "UPDATE_USER")
  @Size(max = 50)
  String updateUser;

  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  @PastOrPresent
  @Column(name = "UPDATE_DATE")
  LocalDateTime updateDate;

}
