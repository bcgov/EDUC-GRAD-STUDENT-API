package ca.bc.gov.educ.api.gradstudent.scheduler;

import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.service.event.EventHandlerService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.DB_COMMITTED;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.ASSESSMENT_STUDENT_UPDATE;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.UPDATE_STUDENT;

/**
 * This class is responsible to check the GRAD_STATUS_EVENT table periodically and publish messages to JET STREAM; if some them are not yet published
 * this is a very edge case scenario which will occur.
 */
@Component
@Slf4j
public class JetStreamEventScheduler {

  private final GradStatusEventRepository gradStatusEventRepository;
  private final Publisher publisher;
  private final EventHandlerService eventHandlerService;
  private final EducGradStudentApiConstants constants;

  /**
   * Instantiates a new Stan event scheduler.
   *
   * @param gradStatusEventRepository the grad status event repository
   * @param publisher                 the publisher
   */
  public JetStreamEventScheduler(final GradStatusEventRepository gradStatusEventRepository,
                                 final Publisher publisher, EventHandlerService eventHandlerService,
                                 final EducGradStudentApiConstants constants) {
    this.gradStatusEventRepository = gradStatusEventRepository;
    this.publisher = publisher;
    this.eventHandlerService = eventHandlerService;
    this.constants = constants;
  }

  /**
   * Find and publish grad status events to stan.
   */
  @Scheduled(cron = "${cron.scheduled.process.events.stan.run}")
  @SchedulerLock(name = "PUBLISH_GRAD_STATUS_EVENTS_TO_JET_STREAM", lockAtLeastFor = "${cron.scheduled.process.events.stan.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.stan.lockAtMostFor}")
  public void findAndPublishGradStatusEventsToJetStream() {
    log.debug("PUBLISH_GRAD_STATUS_EVENTS_TO_JET_STREAM: started - cron {}, lockAtMostFor {}", constants.getGradToTraxCronRun(), constants.getGradToTraxLockAtMostFor());
    LockAssert.assertLocked();
    log.info("Fired jet stream choreography scheduler");
    var gradSchoolEventTypes = Arrays.asList(EventType.UPDATE_SCHOOL_OF_RECORD.toString(), EventType.GRAD_STUDENT_GRADUATED.toString(), EventType.GRAD_STUDENT_UPDATED.toString(), EventType.GRAD_STUDENT_UNDO_COMPLETION.toString());
    var results = gradStatusEventRepository.findByEventStatusAndEventTypeIn(DB_COMMITTED.toString(), gradSchoolEventTypes);
    if (!results.isEmpty()) {
      results.forEach(el -> {
        if (el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5))) {
          try {
            publisher.dispatchChoreographyEvent(el);
          } catch (final Exception ex) {
            log.error("Exception while trying to publish message", ex);
          }
        }
      });
    }

    final var resultsForIncoming = this.gradStatusEventRepository.findAllByEventStatusAndCreateDateBeforeAndEventTypeNotInOrderByCreateDate(DB_COMMITTED.toString(), LocalDateTime.now().minusMinutes(1), 500, gradSchoolEventTypes);
    if (!resultsForIncoming.isEmpty()) {
      log.info("Found {} choreographed events which needs to be processed.", resultsForIncoming.size());

      resultsForIncoming.forEach(event -> {
        switch (event.getEventType()) {
          case "ASSESSMENT_STUDENT_UPDATE":
            this.eventHandlerService.handleAssessmentUpdatedDataEvent(event);
            break;
          case "UPDATE_STUDENT":
            this.eventHandlerService.handleStudentUpdatedDataEvent(event);
            break;
        }
      });
      
    }
  }
}
