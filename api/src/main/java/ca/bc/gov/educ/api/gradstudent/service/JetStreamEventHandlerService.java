package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.MESSAGE_PUBLISHED;

/**
 * This class will process events from Jet Stream, which is used in choreography pattern, where messages are published if a student is created or updated.
 */
@Service
@Slf4j
public class JetStreamEventHandlerService {

  private final GradStatusEventRepository gradStatusEventRepository;


  /**
   * Instantiates a new Stan event handler service.
   *
   * @param gradStatusEventRepository the grad status event repository
   */
  @Autowired
  public JetStreamEventHandlerService(GradStatusEventRepository gradStatusEventRepository) {
    this.gradStatusEventRepository = gradStatusEventRepository;
  }

  /**
   * Update event status.
   *
   * @param choreographedEvent the choreographed event
   */
  @Transactional
  public void updateEventStatus(ChoreographedEvent choreographedEvent) {
    if (choreographedEvent != null && choreographedEvent.getEventID() != null) {
      var eventID = UUID.fromString(choreographedEvent.getEventID());
      var eventOptional = gradStatusEventRepository.findById(eventID);
      if (eventOptional.isPresent()) {
        var studentEvent = eventOptional.get();
        studentEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
        gradStatusEventRepository.save(studentEvent);
      }
    }
  }
}
