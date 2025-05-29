package ca.bc.gov.educ.api.gradstudent.service.event;

import ca.bc.gov.educ.api.gradstudent.constant.EventStatus;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventType;
import ca.bc.gov.educ.api.gradstudent.model.dto.ChoreographedEvent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.CourseStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.DemographicStudent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.MESSAGE_PUBLISHED;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("java:S3864")
public class EventHandlerService {

    /**
     * The constant PAYLOAD_LOG.
     */
    public static final String PAYLOAD_LOG = "payload is :: {}";
    /**
     * The constant EVENT_PAYLOAD.
     */

    private final GraduationStudentRecordService graduationStudentRecordService;
    private final GradStatusEventRepository gradStatusEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public byte[] handleProcessStudentDemDataEvent(Event event) throws JsonProcessingException {
        final DemographicStudent demStudent = JsonUtil.getJsonObjectFromString(DemographicStudent.class, event.getEventPayload());
        var studentFromApi = graduationStudentRecordService.getStudentByPenFromStudentAPI(demStudent.getPen());
        Optional<GraduationStudentRecordEntity> student = graduationStudentRecordService.getStudentByStudentID(studentFromApi.getStudentID());
        log.debug("handleProcessStudentDemDataEvent found student :: {}", student);

        if(student.isPresent()) {
            graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, student.get());
        } else {
            graduationStudentRecordService.createNewStudentRecord(demStudent, studentFromApi);
        }
        event.setEventOutcome(EventOutcome.DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
        val studentEvent = createEventRecord(event);
        return createResponseEvent(studentEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public byte[] handleProcessStudentCourseDataEvent(Event event) throws JsonProcessingException {
        final CourseStudent courseStudent = JsonUtil.getJsonObjectFromString(CourseStudent.class, event.getEventPayload());
        var studentFromApi = graduationStudentRecordService.getStudentByPenFromStudentAPI(courseStudent.getPen());
        Optional<GraduationStudentRecordEntity> student = graduationStudentRecordService.getStudentByStudentID(studentFromApi.getStudentID());
        log.debug("handleProcessStudentCourseDataEvent found student :: {}", student);

        graduationStudentRecordService.handleStudentCourseRecord(student.get(), courseStudent, studentFromApi);
        event.setEventOutcome(EventOutcome.COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
        val studentEvent = createEventRecord(event);
        return createResponseEvent(studentEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleAssessmentUpdatedDataEvent(GradStatusEvent event) throws JsonProcessingException {
        val eventFromDBOptional = this.gradStatusEventRepository.findById(event.getEventId());
        if (eventFromDBOptional.isPresent()) {
            val eventFromDB = eventFromDBOptional.get();
            if (eventFromDB.getEventStatus().equals(EventStatus.DB_COMMITTED.toString())) {
                log.info("Processing event with event ID :: {}", event.getEventId());
                try {
                    switch (event.getEventType()) {
                        case "ASSESSMENT_STUDENT_UPDATE":
                            final String studentID = JsonUtil.getJsonObjectFromString(String.class, event.getEventPayload());
                            Optional<GraduationStudentRecordEntity> student = graduationStudentRecordService.getStudentByStudentID(studentID);
                            if(student.isPresent()) {
                                graduationStudentRecordService.handleAssessmentUpdateEvent(student.get(), event);
                                updateEvent(event);
                            } else {
                                log.info("Student does not exist in GRAD :: {}", event.getEventId());
                            }
                            break;
                        default:
                            log.warn("Silently ignoring event: {}", event);
                            break;
                    }
                    log.info("Event was processed, ID :: {}", event.getEventId());
                } catch (final Exception exception) {
                    log.error("Exception while processing event :: {}", event, exception);
                }
            }
        }
    }

    private GradStatusEvent createEventRecord(Event event) {
        return GradStatusEvent.builder()
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .createUser(EducGradStudentApiConstants.DEFAULT_CREATED_BY)
                .updateUser(EducGradStudentApiConstants.DEFAULT_UPDATED_BY)
                .eventPayload(event.getEventPayload())
                .eventType(event.getEventType().toString())
                .sagaId(event.getSagaId())
                .eventStatus(MESSAGE_PUBLISHED.toString())
                .eventOutcome(event.getEventOutcome().toString())
                .replyChannel(event.getReplyTo())
                .build();
    }

    private byte[] createResponseEvent(GradStatusEvent event) throws JsonProcessingException {
        val responseEvent = Event.builder()
                .sagaId(event.getSagaId())
                .eventType(EventType.valueOf(event.getEventType()))
                .eventOutcome(EventOutcome.valueOf(event.getEventOutcome()))
                .eventPayload(event.getEventPayload()).build();
        return JsonUtil.getJsonBytesFromObject(responseEvent);
    }

    protected void updateEvent(final GradStatusEvent event) {
        this.gradStatusEventRepository.findByEventId(event.getEventId()).ifPresent(existingEvent -> {
            existingEvent.setEventStatus(EventStatus.PROCESSED.toString());
            existingEvent.setUpdateDate(LocalDateTime.now());
            this.gradStatusEventRepository.save(existingEvent);
        });
    }

}
