package ca.bc.gov.educ.api.gradstudent.scheduler;

import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class PurgeOldRecordsScheduler {
    private final GradStatusEventRepository gradStatusEventRepository;
    private final EducGradStudentApiConstants constants;

    public PurgeOldRecordsScheduler(final GradStatusEventRepository gradStatusEventRepository,
                                    final EducGradStudentApiConstants constants) {
        this.gradStatusEventRepository = gradStatusEventRepository;
        this.constants = constants;
    }

    @Scheduled(cron = "${cron.scheduled.process.purge-old-records.run}")
    @SchedulerLock(name = "PurgeOldRecordsLock",
            lockAtLeastFor = "PT1H", lockAtMostFor = "PT1H") //midnight job so lock for an hour
    @Transactional
    public void purgeOldRecords() {
        LockAssert.assertLocked();
        final LocalDateTime createDateToCompare = this.calculateCreateDateBasedOnStaleEventInDays();
        this.gradStatusEventRepository.deleteByCreateDateBefore(createDateToCompare);
    }

    private LocalDateTime calculateCreateDateBasedOnStaleEventInDays() {
        final LocalDateTime currentTime = LocalDateTime.now();
        return currentTime.minusDays(this.constants.getRecordsStaleInDays());
    }
}
