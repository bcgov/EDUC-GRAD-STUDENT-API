package ca.bc.gov.educ.api.gradstudent.model.dto;

import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import lombok.Data;

/**
 * The type Choreographed event.
 */
@Data
public class ChoreographedEvent {
  /**
   * The Event id.
   */
  String eventID; // the primary key of student event table.
  /**
   * The Event type.
   */
  EventType eventType;
  /**
   * The Event outcome.
   */
  EventOutcome eventOutcome;
  /**
   * The Event payload.
   */
  String eventPayload;
  /**
   * The Create user.
   */
  String createUser;
  /**
   * The Update user.
   */
  String updateUser;
}
