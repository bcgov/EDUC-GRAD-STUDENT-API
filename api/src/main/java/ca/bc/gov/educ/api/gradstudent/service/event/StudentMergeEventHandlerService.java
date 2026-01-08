package ca.bc.gov.educ.api.gradstudent.service.event;

import ca.bc.gov.educ.api.gradstudent.constant.EventStatus;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.penservices.v1.StudentMerge;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.service.StudentMergeService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
@Slf4j
@AllArgsConstructor
public class StudentMergeEventHandlerService {

    private final StudentMergeService studentMergeService;
    private final GradStatusEventRepository gradStatusEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Boolean processMergeEvent(final GradStatusEvent event) throws JsonProcessingException {
        log.info("processing merge event {}", event);
        final List<StudentMerge> studentMerges = new ObjectMapper().readValue(event.getEventPayload(), new TypeReference<>() {
        });
        Optional<StudentMerge> studentMerge = studentMerges.stream().filter(this::mergeToPredicate).findFirst();
        if (studentMerge.isEmpty()) {
            log.info("Student Merge is empty. EventId: {}", event.getEventId());
            markEventAsProcessed(event.getEventId());
            return true;
        }
        boolean mergeResult = studentMergeService.mergeStudentProcess(UUID.fromString(studentMerge.get().getStudentID()), UUID.fromString(studentMerge.get().getMergeStudentID()));
        if (mergeResult) {
            markEventAsProcessed(event.getEventId());
        }
        return mergeResult;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Boolean processDeMergeEvent(final GradStatusEvent event) throws JsonProcessingException {
        log.info("processing de-merge event {}", event);
        final List<StudentMerge> studentDeMerges = new ObjectMapper().readValue(event.getEventPayload(), new TypeReference<>() {
        });
        Optional<StudentMerge> studentDeMerge = studentDeMerges.stream().filter(this::mergeToPredicate).findFirst();
        if (studentDeMerge.isEmpty()) {
            log.info("Student Demerge is empty. EventId: {}", event.getEventId());
        }else {
            studentMergeService.demergeStudentProcess(UUID.fromString(studentDeMerge.get().getStudentID()));
        }
        markEventAsProcessed(event.getEventId());
        return true;
    }

    private void markEventAsProcessed(UUID eventId) {
        gradStatusEventRepository.findByEventId(eventId).ifPresent(this::markRecordAsProcessed);
    }

    private boolean mergeToPredicate(final StudentMerge studentMerge) {
        return StringUtils.equals(studentMerge.getStudentMergeDirectionCode(), "TO");
    }

    private void markRecordAsProcessed(final GradStatusEvent event) {
        event.setEventStatus(EventStatus.PROCESSED.toString());
        event.setUpdateDate(LocalDateTime.now());
        this.gradStatusEventRepository.save(event);
        log.info("event processed {}", event.getEventId());
    }
}
