package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.HistoryActivityCodes;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentNote;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import com.nimbusds.oauth2.sdk.util.CollectionUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    private final CommonService commonService;
    private final GraduationStudentRecordRepository graduationStatusRepository;
    private final GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository;
    private final HistoryService historyService;

    private static final String MERGED_STATUS_CODE = "MER";

    @Transactional(propagation = Propagation.REQUIRED)
    public boolean mergeStudentProcess(UUID studentID, UUID trueStudentID) {
        //Check the student present in Grad System
        Optional<GraduationStudentRecordEntity> graduationStudentRecordEntityOptional = graduationStatusRepository.findOptionalByStudentID(studentID);
        if (graduationStudentRecordEntityOptional.isEmpty()) {
            log.warn("Student merge request for student with ID {} does not exist.", studentID);
            return true;
        }
        GraduationStudentRecordEntity graduationStudentRecordEntity = graduationStudentRecordEntityOptional.get();
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

    @Transactional(propagation = Propagation.REQUIRED)
    public void demergeStudentProcess(UUID studentID) {
        //Check the student present in Grad System
        Optional<GraduationStudentRecordEntity> graduationStudentRecordEntityOptional = graduationStatusRepository.findOptionalByStudentID(studentID);
        if (graduationStudentRecordEntityOptional.isEmpty()) {
            log.warn("Student demerge request for student with ID {} does not exist.", studentID);
        }else {
            GraduationStudentRecordEntity graduationStudentRecordEntity = graduationStudentRecordEntityOptional.get();
            var priorStatus = findMERRecordWithPrevious(studentID);
            //Update the grad status for Source Student
            graduationStudentRecordEntity.setStudentStatus(priorStatus);
            graduationStudentRecordEntity.setUpdateUser(ThreadLocalStateUtil.getCurrentUser());
            graduationStudentRecordEntity.setUpdateDate(LocalDateTime.now());
            graduationStatusRepository.save(graduationStudentRecordEntity);
            // update history
            historyService.createStudentHistory(graduationStudentRecordEntity, HistoryActivityCodes.USERDEMERGE.getCode());
        }
    }

    public String findMERRecordWithPrevious(UUID studentID) {
        List<GraduationStudentRecordHistoryEntity> allRecords = graduationStudentRecordHistoryRepository.findAllHistoryDescByStudentId(studentID);

        GraduationStudentRecordHistoryEntity result = null;

        for (int i = 0; i < allRecords.size(); i++) {
            if ("MER".equals(allRecords.get(i).getStudentStatus())) {
                if (i + 1 < allRecords.size()) {
                    result = allRecords.get(i + 1);
                }
                break;
            }
        }

        if(result != null){
            return result.getStudentStatus();
        }

        return "CUR";
    }



}
