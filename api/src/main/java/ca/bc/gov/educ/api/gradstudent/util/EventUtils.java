package ca.bc.gov.educ.api.gradstudent.util;

import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.exception.IgnoreEventException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventValidation;
import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEventValidation;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;


public final class EventUtils {
  private EventUtils() {
  }

  public static ChoreographedEvent getChoreographedEventIfValid(String eventString) throws JsonProcessingException, IgnoreEventException {
    final ChoreographedEventValidation event = JsonUtil.getJsonObjectFromString(ChoreographedEventValidation.class, eventString);
    if(StringUtils.isNotBlank(event.getEventOutcome()) && !EventOutcome.isValid(event.getEventOutcome())) {
      throw new IgnoreEventException("Invalid event outcome", event.getEventType(), event.getEventOutcome());
    }else if(StringUtils.isNotBlank(event.getEventType()) && !EventType.isValid(event.getEventType())) {
      throw new IgnoreEventException("Invalid event type", event.getEventType(), event.getEventOutcome());
    }
    return JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
  }

  public static Event getEventIfValid(String eventString) throws JsonProcessingException, IgnoreEventException {
    final EventValidation event = JsonUtil.getJsonObjectFromString(EventValidation.class, eventString);
    if(StringUtils.isNotBlank(event.getEventType()) && !EventType.isValid(event.getEventType())) {
      throw new IgnoreEventException("Invalid event type", event.getEventType(), event.getEventOutcome());
    }
    return JsonUtil.getJsonObjectFromString(Event.class, eventString);
  }

}
