package ca.bc.gov.educ.api.gradstudent.scheduler;

import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RefreshNonGradStatusScheduler {
    private final GraduationStudentRecordRepository graduationStudentRecordRepository;

    @Autowired
    public RefreshNonGradStatusScheduler(GraduationStudentRecordRepository graduationStudentRecordRepository) {
        this.graduationStudentRecordRepository = graduationStudentRecordRepository;
    }

    @Scheduled(cron = "${cron.scheduled.process.refresh-non-grad-status.run}")
    @SchedulerLock(name = "refreshNonGradStatusLock",
            lockAtLeastFor = "PT2M", lockAtMostFor = "PT5M")
    @Transactional
    public void refreshNonGradStatuses() {
        LockAssert.assertLocked();
        graduationStudentRecordRepository.updateGradStudentRecalcFlagsForCurrentStudentsWithNullCompletion();
    }


}
