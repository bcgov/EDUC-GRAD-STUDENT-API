package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.HistoryActivityCodes;
import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentNote;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class StudentMergeService {

    private final GraduationStatusService graduationStatusService;
    private final CommonService commonService;
    private final GraduationStudentRecordRepository graduationStatusRepository;
    private final Publisher publisher;
    private final HistoryService historyService;

    private static final String MERGED_STATUS_CODE = "MER";
    private static final Logger logger = LoggerFactory.getLogger(StudentMergeService.class);

    @Transactional(propagation = Propagation.REQUIRED)
    public Boolean mergeStudentProcess(UUID studentID, UUID trueStudentID) throws JsonProcessingException {
        //Check the student present in Grad System
        Optional<GraduationStudentRecordEntity> graduationStudentRecordEntityOptional = graduationStatusRepository.findOptionalByStudentID(studentID);
        if (graduationStudentRecordEntityOptional.isEmpty()) {
            log.warn("Student merge request for student with ID {} does not exist.", studentID);
            return true;
        }
        GraduationStudentRecordEntity graduationStudentRecordEntity = graduationStudentRecordEntityOptional.get();
        //Check the merged student present in Grad system; if not onboard.
        this.checkIfExistsAndOnboard(trueStudentID);
        //Update the grad status for Source Student
        graduationStudentRecordEntity.setStudentStatus(MERGED_STATUS_CODE);
        graduationStudentRecordEntity.setUpdateUser(ThreadLocalStateUtil.getCurrentUser());
        graduationStudentRecordEntity.setUpdateDate(LocalDateTime.now());
        graduationStatusRepository.save(graduationStudentRecordEntity);
        // update history
        historyService.createStudentHistory(graduationStudentRecordEntity, HistoryActivityCodes.USERMERGE.getCode());
        //Copy Notes : If exists in Source Student, copy to Target Student
        List<StudentNote> studentNoteList = this.commonService.getAllStudentNotes(studentID);
        if (CollectionUtils.isNotEmpty(studentNoteList)) {
            studentNoteList.forEach(note -> {
                note.setStudentID(trueStudentID.toString());
                note.setId(UUID.randomUUID());
            });
            this.commonService.saveStudentNotes(studentNoteList);
        }
        return true;
    }

    private void checkIfExistsAndOnboard(UUID studentID) throws JsonProcessingException {
        boolean isExists = isStudentPresentInGrad(studentID);
        if (!isExists) {
            logger.info("Student with ID {} does not exist in Grad System. Adopting student", studentID);
            var pair = this.graduationStatusService.adoptStudent(studentID, ThreadLocalStateUtil.getCurrentUser());
            if (pair.getRight() != null) {
                publisher.dispatchChoreographyEvent(pair.getRight());
            }
        }
    }

    private boolean isStudentPresentInGrad(UUID studentID) {
        return graduationStatusRepository.existsByStudentID(studentID);
    }

}
