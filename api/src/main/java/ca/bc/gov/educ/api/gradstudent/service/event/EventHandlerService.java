package ca.bc.gov.educ.api.gradstudent.service.event;

import ca.bc.gov.educ.api.gradstudent.constant.EventStatus;
import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.exception.ServiceException;
import ca.bc.gov.educ.api.gradstudent.model.dc.Event;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.model.dc.EventType;
import ca.bc.gov.educ.api.gradstudent.model.dto.Course;
import ca.bc.gov.educ.api.gradstudent.model.dto.GradStudentUpdateResult;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.algorithm.v1.StudentCourseAlgorithmData;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.assessment.v1.StudentForAssessmentUpdate;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.CourseStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.DemographicStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.student.v1.StudentUpdate;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.mapper.StudentCourseAlgorithmDataMapper;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCourseRepository;
import ca.bc.gov.educ.api.gradstudent.service.CourseService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EventUtil;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.util.Pair;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.ASSESSMENT_STUDENT_UPDATE;
import static ca.bc.gov.educ.api.gradstudent.constant.EventType.UPDATE_STUDENT;
import static ca.bc.gov.educ.api.gradstudent.model.dc.EventOutcome.GRAD_STUDENT_CITIZENSHIP_UPDATED;
import static ca.bc.gov.educ.api.gradstudent.model.dc.EventOutcome.SCHOOL_OF_RECORD_UPDATED;
import static ca.bc.gov.educ.api.gradstudent.model.dc.EventType.UPDATE_GRAD_STUDENT_CITIZENSHIP;
import static ca.bc.gov.educ.api.gradstudent.model.dc.EventType.UPDATE_SCHOOL_OF_RECORD;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@SuppressWarnings("java:S3864")
public class EventHandlerService {

    public static final String PAYLOAD_LOG = "payload is :: {}";
    private static final StudentCourseAlgorithmDataMapper STUDENT_COURSE_ALGORITHM_DATA_MAPPER = StudentCourseAlgorithmDataMapper.mapper;
    private final GraduationStudentRecordService graduationStudentRecordService;
    private final CourseService courseService;
    private final GradStatusEventRepository gradStatusEventRepository;
    private final StudentCourseRepository studentCourseRepository;
    private final GraduationStatusTransformer graduationStatusTransformer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Pair<byte[], List<GradStatusEvent>> handleProcessStudentDemDataEvent(Event event) throws JsonProcessingException {
        final DemographicStudent demStudent = JsonUtil.getJsonObjectFromString(DemographicStudent.class, event.getEventPayload());
        var studentFromApi = graduationStudentRecordService.getStudentByPenFromStudentAPI(demStudent.getPen());
        log.debug("Student response from API is: {}", studentFromApi);
        Optional<GraduationStudentRecordEntity> student = graduationStudentRecordService.getStudentByStudentID(studentFromApi.getStudentID());
        log.debug("handleProcessStudentDemDataEvent found student :: {}", student);

        List<GradStatusEvent> gradStatusEventList = new ArrayList<>();
        GradStudentUpdateResult gradStudentUpdateResult = null;
        GraduationStudentRecordEntity  graduationStudentRecordEntity;
        if(student.isPresent()) {
            var result = graduationStudentRecordService.updateStudentRecord(demStudent, studentFromApi, student.get());
            gradStudentUpdateResult = result.getLeft();
            graduationStudentRecordEntity = result.getRight();
        } else {
            graduationStudentRecordEntity = graduationStudentRecordService.createNewStudentRecord(demStudent, studentFromApi);
        }
        
        if(gradStudentUpdateResult != null) {
            if(gradStudentUpdateResult.isSchoolOfRecordUpdated()) {
                var studentForUpdate = StudentForAssessmentUpdate
                        .builder()
                        .studentID(studentFromApi.getStudentID())
                        .schoolOfRecordID(demStudent.getSchoolID())
                        .build();
                var gradStatusEvent = EventUtil.createEvent(demStudent.getCreateUser(),
                        demStudent.getUpdateUser(), JsonUtil.getJsonStringFromObject(studentForUpdate), UPDATE_SCHOOL_OF_RECORD, SCHOOL_OF_RECORD_UPDATED);
                gradStatusEventRepository.save(gradStatusEvent);
                gradStatusEventList.add(gradStatusEvent);
            }
            
            if(gradStudentUpdateResult.isCitizenshipUpdated()) {
                var gradStudent = graduationStatusTransformer.transformToDTO(graduationStudentRecordEntity);
                var gradStatusEvent = EventUtil.createEvent(demStudent.getCreateUser(),
                        demStudent.getUpdateUser(), JsonUtil.getJsonStringFromObject(gradStudent), UPDATE_GRAD_STUDENT_CITIZENSHIP, GRAD_STUDENT_CITIZENSHIP_UPDATED);
                gradStatusEventRepository.save(gradStatusEvent);
                gradStatusEventList.add(gradStatusEvent);
            }
        }

        event.setEventOutcome(EventOutcome.DEM_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
        val studentEvent = createEventRecord(event);
        return Pair.of(createResponseEvent(studentEvent), gradStatusEventList);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public byte[] handleProcessStudentCourseDataEvent(Event event) throws JsonProcessingException {
        CourseStudent courseStudent = objectMapper.readValue(event.getEventPayload(), new TypeReference<>() {
        });
        var studentFromApi = graduationStudentRecordService.getStudentByPenFromStudentAPI(courseStudent.getPen());
        Optional<GraduationStudentRecordEntity> student = graduationStudentRecordService.getStudentByStudentID(studentFromApi.getStudentID());
        log.debug("handleProcessStudentCourseDataEvent found student :: {}", student);

        graduationStudentRecordService.handleStudentCourseRecord(student.get(), courseStudent, studentFromApi);
        event.setEventOutcome(EventOutcome.COURSE_STUDENT_PROCESSED_IN_GRAD_STUDENT_API);
        val studentEvent = createEventRecord(event);
        return createResponseEvent(studentEvent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GradStatusEvent handleAssessmentUpdatedDataEvent(GradStatusEvent event) {
        val eventFromDBOptional = this.gradStatusEventRepository.findById(event.getEventId());
        if (eventFromDBOptional.isPresent()) {
            val eventFromDB = eventFromDBOptional.get();
            if (eventFromDB.getEventStatus().equals(EventStatus.DB_COMMITTED.toString())) {
                log.info("Processing event with event ID :: {}", event.getEventId());
                try {
                    if (event.getEventType().equals(ASSESSMENT_STUDENT_UPDATE.toString())) {
                        final String studentID = JsonUtil.getJsonObjectFromString(String.class, event.getEventPayload());
                        Optional<GraduationStudentRecordEntity> student = graduationStudentRecordService.getStudentByStudentID(studentID);
                        if (student.isPresent()) {
                            graduationStudentRecordService.handleSetFlagsForGradStudent(student.get(), event);
                            updateEvent(event);
                        } else {
                            var adoptEvent = graduationStudentRecordService.handleAssessmentAdoptEvent(studentID, event);
                            updateEvent(event);
                            log.info("Event was processed, ID :: {}", event.getEventId());
                            return adoptEvent;
                        }
                    } else {
                        log.warn("Silently ignoring event: {}", event);
                    }
                    log.info("Event was processed, ID :: {}", event.getEventId());
                } catch (final Exception exception) {
                    log.error("Exception while processing event :: {}", event, exception);
                }
            }
        }
        return null;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStudentUpdatedDataEvent(GradStatusEvent event) {
        val eventFromDBOptional = this.gradStatusEventRepository.findById(event.getEventId());
        if (eventFromDBOptional.isPresent()) {
            val eventFromDB = eventFromDBOptional.get();
            if (eventFromDB.getEventStatus().equals(EventStatus.DB_COMMITTED.toString())) {
                log.info("Processing event with event ID :: {}", event.getEventId());
                try {
                    if (event.getEventType().equals(UPDATE_STUDENT.toString())) {
                        final StudentUpdate studentUpdate = JsonUtil.getJsonObjectFromString(StudentUpdate.class, event.getEventPayload());
                        Optional<GraduationStudentRecordEntity> student = graduationStudentRecordService.getStudentByStudentID(studentUpdate.getStudentID());
                        if (student.isPresent()) {
                            graduationStudentRecordService.handleSetFlagsForGradStudent(student.get(), event);
                        }
                        updateEvent(event);
                    } else {
                        log.warn("Silently ignoring event: {}", event);
                    }
                    log.info("Event was processed, ID :: {}", event.getEventId());
                } catch (final Exception exception) {
                    log.error("Exception while processing event :: {}", event, exception);
                }
            }
        }
    }

    public byte[] handleGetStudentCoursesEvent(Event event) {
        val studentCourseEntityList = studentCourseRepository.findByStudentID(UUID.fromString(event.getEventPayload()));
        log.info("Found :: {} assessment student records for student ID :: {}", studentCourseEntityList.size(), UUID.fromString(event.getEventPayload()));
        if (!studentCourseEntityList.isEmpty()) {
            try {
                var studentCourses = studentCourseEntityList.stream().map(STUDENT_COURSE_ALGORITHM_DATA_MAPPER::toStructure).collect(Collectors.toList());

                List<StudentCourseAlgorithmData> enhancedStudentCourses = enhanceStudentCoursesWithCourseDetails(studentCourses);
                return JsonUtil.getJsonBytesFromObject(enhancedStudentCourses);
            } catch (Exception e) {
                log.error("Error enhancing student courses with course details: {}", e.getMessage(), e);
                return new byte[0];
            }
        } else {
            return new byte[0];
        }
    }

    private List<StudentCourseAlgorithmData> enhanceStudentCoursesWithCourseDetails(List<StudentCourseAlgorithmData> studentCourses) {
        List<String> courseIDs = studentCourses.stream()
            .flatMap(studentCourse -> Stream.of(
                studentCourse.getCourseCode(),
                studentCourse.getRelatedCourse()
            ))
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (courseIDs.isEmpty()) {
            log.error("No valid course IDs found in student courses");
            throw new ServiceException("No valid course IDs found in student courses");
        }

        // Get course details from CourseService
        List<Course> courseDetails = courseService.getCourses(courseIDs);

        if (courseDetails.isEmpty()) {
            log.error("No course details found for course IDs: {}", courseIDs);
            throw new EntityNotFoundException("No course details found for course IDs: " + courseIDs);
        }

        // Create a map for quick lookup
        Map<String, Course> courseMap = courseDetails.stream()
            .collect(Collectors.toMap(Course::getCourseID, course -> course));

        // Check if all course IDs were found
        List<String> missingCourseIDs = courseIDs.stream()
            .filter(courseID -> !courseMap.containsKey(courseID))
            .toList();

        if (!missingCourseIDs.isEmpty()) {
            log.error("Missing course details for course IDs: {}", missingCourseIDs);
            throw new ServiceException("Missing course details for course IDs: " + missingCourseIDs);
        }

        // Enhance each student course with course details
        return studentCourses.stream()
            .peek(studentCourse -> {
                if(studentCourse.getRelatedCourse() != null) {
                    Course courseDetail = courseMap.get(studentCourse.getRelatedCourse());
                    studentCourse.setRelatedCourse(courseDetail.getCourseCode());
                    studentCourse.setRelatedCourseName(courseDetail.getCourseName());
                }
                if (studentCourse.getCourseCode() != null) {
                    Course courseDetail = courseMap.get(studentCourse.getCourseCode());
                    studentCourse.setCourseCode(courseDetail.getCourseCode());
                    studentCourse.setCourseName(courseDetail.getCourseName());
                    studentCourse.setOriginalCredits(courseDetail.getNumCredits());
                    studentCourse.setCourseLevel(courseDetail.getCourseLevel());
                    studentCourse.setGenericCourseType(courseDetail.getGenericCourseType());
                    studentCourse.setCourseName(courseDetail.getCourseName());
                    studentCourse.setLanguage(courseDetail.getLanguage());
                } else {
                    throw new ServiceException("Student course missing course information");
                }
            })
            .toList();
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
