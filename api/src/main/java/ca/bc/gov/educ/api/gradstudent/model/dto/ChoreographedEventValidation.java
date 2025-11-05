package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * The type Choreographed event.
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ChoreographedEventValidation {
  /**
   * The Event id.
   */
  String eventID; // the primary key of student event table.
  /**
   * The Event type.
   */
  String eventType;
  /**
   * The Event outcome.
   */
  String eventOutcome;
  /**
   * The Activity code.
   */
  String activityCode;
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
