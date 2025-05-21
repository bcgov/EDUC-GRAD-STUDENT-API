package ca.bc.gov.educ.api.gradstudent.cache;

import ca.bc.gov.educ.api.gradstudent.service.CourseCacheService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Profile("!test")
@Slf4j
@Component
public class CacheInitializer {
    private final CourseCacheService courseCacheService;

    public CacheInitializer(CourseCacheService courseCacheService) {
        this.courseCacheService = courseCacheService;
    }

    @PostConstruct
    public void loadCacheOnStartup() {
        log.info("Initializing cache at startup...");
        courseCacheService.loadExaminableCourses();
        courseCacheService.loadLetterGrades();
    }

    @Scheduled(cron = "${cron.scheduled.process.refresh-course-details.run}")
    public void scheduledCacheRefresh() {
        log.info("Refreshing cache...");
        courseCacheService.loadExaminableCourses();
        courseCacheService.loadLetterGrades();
    }
}
