package ca.bc.gov.educ.api.gradstudent.scheduler;

import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.DB_COMMITTED;

/**
 * This class is responsible to check the GRAD_STATUS_EVENT table periodically and publish messages to JET STREAM; if some them are not yet published
 * this is a very edge case scenario which will occur.
 */
@Component
@Slf4j
public class JetStreamEventScheduler {

  private final GradStatusEventRepository gradStatusEventRepository;
  private final Publisher publisher;

  private final EducGradStudentApiConstants constants;

  /**
   * Instantiates a new Stan event scheduler.
   *
   * @param gradStatusEventRepository the grad status event repository
   * @param publisher                 the publisher
   */
  public JetStreamEventScheduler(final GradStatusEventRepository gradStatusEventRepository,
                                 final Publisher publisher,
                                 final EducGradStudentApiConstants constants) {
    this.gradStatusEventRepository = gradStatusEventRepository;
    this.publisher = publisher;
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
    var results = gradStatusEventRepository.findByEventStatusOrderByCreateDate(DB_COMMITTED.toString());
    if (!results.isEmpty()) {
      int cnt = 0;
      for (GradStatusEvent el : results) {
        if (cnt++ >= constants.getGradToTraxProcessingThreshold()) {
          log.info(" ==> Reached the processing threshold of {}", constants.getGradToTraxProcessingThreshold());
          break;
        }
        try {
          publisher.dispatchChoreographyEvent(el);
        } catch (final Exception ex) {
          log.error("Exception while trying to publish message", ex);
        }
      }
      log.debug("PUBLISH_GRAD_STATUS_EVENTS_TO_JET_STREAM: processing is completed");
    }
  }
}
