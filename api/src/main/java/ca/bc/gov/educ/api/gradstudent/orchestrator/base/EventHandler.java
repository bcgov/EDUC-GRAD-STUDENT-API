package ca.bc.gov.educ.api.gradstudent.orchestrator.base;

import ca.bc.gov.educ.api.gradstudent.model.dc.Event;

import java.io.IOException;
import java.util.concurrent.TimeoutException;


/**
 * The interface event handler.
 */
public interface EventHandler {

  void handleEvent(Event event) throws InterruptedException, IOException, TimeoutException;

  String getTopicToSubscribe();
}
