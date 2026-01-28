package ca.bc.gov.educ.api.gradstudent.config;

import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.ThreadFactoryBuilder;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.Duration;
import java.util.concurrent.Executor;

@Configuration
@EnableAsync
@Profile("!test")
public class AsyncConfig {

    @Bean(name = "subscriberExecutor")
    @Autowired
    public Executor threadPoolTaskExecutor(final EducGradStudentApiConstants educGradStudentApiConstants) {
        return new EnhancedQueueExecutor.Builder()
                .setThreadFactory(new ThreadFactoryBuilder().withNameFormat("message-subscriber-%d").get())
                .setCorePoolSize(educGradStudentApiConstants.getMinSubscriberThreads()).setMaximumPoolSize(educGradStudentApiConstants.getMaxSubscriberThreads()).setKeepAliveTime(Duration.ofSeconds(60)).build();
    }

    @Bean(name = "cacheExecutor")
    @Autowired
    public Executor cacheExecutor(final EducGradStudentApiConstants educGradStudentApiConstants) {
        return new EnhancedQueueExecutor.Builder()
                .setThreadFactory(new ThreadFactoryBuilder().withNameFormat("cache-executor-%d").get())
                .setCorePoolSize(educGradStudentApiConstants.getMinSubscriberThreads())
                .setMaximumPoolSize(educGradStudentApiConstants.getMaxSubscriberThreads())
                .setKeepAliveTime(Duration.ofSeconds(60))
                .build();
    }

}
