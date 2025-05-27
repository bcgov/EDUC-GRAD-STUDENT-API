package ca.bc.gov.educ.api.gradstudent.util;


import ca.bc.gov.educ.api.gradstudent.constant.EventStatus;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventType;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;

import java.time.LocalDateTime;


public class EventUtil {
  private EventUtil() {
  }

  public static GradStatusEvent createEvent(String createUser, String updateUser, String jsonString, EventType eventType, EventOutcome eventOutcome) {
    return GradStatusEvent.builder()
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .createUser(createUser)
      .updateUser(updateUser)
      .eventPayload(jsonString)
      .eventType(eventType.toString())
      .eventStatus(EventStatus.DB_COMMITTED.toString())
      .eventOutcome(eventOutcome.toString())
      .build();
  }
}
