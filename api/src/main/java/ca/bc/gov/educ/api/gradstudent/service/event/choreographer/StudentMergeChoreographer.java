package ca.bc.gov.educ.api.gradstudent.service.event.choreographer;

import ca.bc.gov.educ.api.gradstudent.constant.EventStatus;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.service.event.StudentMergeEventHandlerService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
@Slf4j
public class StudentMergeChoreographer {

    private final Executor singleTaskExecutor = new EnhancedQueueExecutor.Builder()
            .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("sm-task-executor-%d").build())
            .setCorePoolSize(1).setMaximumPoolSize(1).build();
    private final StudentMergeEventHandlerService studentMergeEventHandlerService;
    private final GradStatusEventRepository gradStatusEventRepository;

    public StudentMergeChoreographer(final StudentMergeEventHandlerService studentMergeEventHandlerService, GradStatusEventRepository gradStatusEventRepository) {
        this.studentMergeEventHandlerService = studentMergeEventHandlerService;
        this.gradStatusEventRepository = gradStatusEventRepository;
    }

    /**
     * Handle event.
     *
     * @param event the event
     */
    public void handleEvent(@NonNull final GradStatusEvent event) {
        //only one thread will process all the request.
        this.singleTaskExecutor.execute(() -> {
            try {
                val eventFromDBOptional = this.gradStatusEventRepository.findById(event.getEventId());
                if (eventFromDBOptional.isPresent()) {
                    val eventFromDB = eventFromDBOptional.get();
                    if (eventFromDB.getEventStatus().equals(EventStatus.DB_COMMITTED.toString())) {
                        log.debug("Processing event with event ID :: {}", event.getEventId());
                        switch (event.getEventType()) {
                            case "CREATE_MERGE":
                                this.studentMergeEventHandlerService.processMergeEvent(event);
                                break;
                            case "DELETE_MERGE":
                                this.studentMergeEventHandlerService.processDeMergeEvent(event);
                                break;
                            default:
                                log.warn("Silently ignoring event: {}", event);
                                break;
                        }
                    }
                }
            } catch (final Exception exception) {
                log.error("Exception while processing event :: {}", event, exception);
            }
        });
    }
}
