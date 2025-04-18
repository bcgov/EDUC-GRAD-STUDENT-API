package ca.bc.gov.educ.api.gradstudent.service;


import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecordHistory;
import ca.bc.gov.educ.api.gradstudent.model.dto.OptionalProgram;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgramHistory;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStudentRecordHistoryTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.StudentOptionalProgramHistoryTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.HistoryActivityRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
public class HistoryService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);

    final
    WebClient webClient;

    final GraduationStudentRecordRepository graduationStatusRepository;
    final GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository;
    final GraduationStudentRecordHistoryTransformer graduationStudentRecordHistoryTransformer;
    final StudentOptionalProgramHistoryRepository studentOptionalProgramHistoryRepository;
    final StudentOptionalProgramHistoryTransformer studentOptionalProgramHistoryTransformer;
    final HistoryActivityRepository historyActivityRepository;
    final EducGradStudentApiConstants constants;

    @Autowired
    public HistoryService(WebClient webClient, GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository, GraduationStudentRecordHistoryTransformer graduationStudentRecordHistoryTransformer, StudentOptionalProgramHistoryRepository studentOptionalProgramHistoryRepository, StudentOptionalProgramHistoryTransformer studentOptionalProgramHistoryTransformer, EducGradStudentApiConstants constants, HistoryActivityRepository historyActivityRepository, GraduationStudentRecordRepository graduationStatusRepository) {
        this.webClient = webClient;
        this.graduationStudentRecordHistoryRepository = graduationStudentRecordHistoryRepository;
        this.graduationStudentRecordHistoryTransformer = graduationStudentRecordHistoryTransformer;
        this.studentOptionalProgramHistoryRepository = studentOptionalProgramHistoryRepository;
        this.studentOptionalProgramHistoryTransformer = studentOptionalProgramHistoryTransformer;
        this.historyActivityRepository = historyActivityRepository;
        this.graduationStatusRepository = graduationStatusRepository;
        this.constants = constants;
    }

    public void createStudentHistory(GraduationStudentRecordEntity curStudentEntity, String historyActivityCode) {
        if (curStudentEntity != null) {
            logger.debug("Create Student History");
            final GraduationStudentRecordHistoryEntity graduationStudentRecordHistoryEntity = new GraduationStudentRecordHistoryEntity();
            BeanUtils.copyProperties(curStudentEntity, graduationStudentRecordHistoryEntity);
            graduationStudentRecordHistoryEntity.setCreateUser(curStudentEntity.getCreateUser());
            graduationStudentRecordHistoryEntity.setCreateDate(curStudentEntity.getCreateDate());
            graduationStudentRecordHistoryEntity.setActivityCode(historyActivityCode);
            graduationStudentRecordHistoryEntity.setStudentGradData("{ EMPTY CLOB }");
            graduationStudentRecordHistoryRepository.saveAndFlush(graduationStudentRecordHistoryEntity);
        }
    }

    public void createStudentOptionalProgramHistory(StudentOptionalProgramEntity curStudentOptionalProgramEntity, String historyActivityCode) {
        logger.debug("Create Student Optional History");
        final StudentOptionalProgramHistoryEntity studentOptionalProgramHistoryEntity = new StudentOptionalProgramHistoryEntity();
        BeanUtils.copyProperties(curStudentOptionalProgramEntity, studentOptionalProgramHistoryEntity);
        studentOptionalProgramHistoryEntity.setStudentOptionalProgramID(curStudentOptionalProgramEntity.getId());
        studentOptionalProgramHistoryEntity.setActivityCode(historyActivityCode);
        studentOptionalProgramHistoryEntity.setStudentOptionalProgramData("{ EMPTY CLOB }");
        studentOptionalProgramHistoryRepository.save(studentOptionalProgramHistoryEntity);
    }

    public List<GraduationStudentRecordHistory> getStudentEditHistory(UUID studentID) {
        List<GraduationStudentRecordHistory> histList = graduationStudentRecordHistoryTransformer.transformToDTO(graduationStudentRecordHistoryRepository.findByStudentID(studentID));
        histList.forEach(gS -> {
            Optional<HistoryActivityCodeEntity> entOpt = historyActivityRepository.findById(gS.getActivityCode());
            entOpt.ifPresent(historyActivityCodeEntity -> gS.setActivityCodeDescription(historyActivityCodeEntity.getDescription()));
        });
        return histList;
    }

    public List<StudentOptionalProgramHistory> getStudentOptionalProgramEditHistory(UUID studentID, String accessToken) {
        List<StudentOptionalProgramHistory> histList = studentOptionalProgramHistoryTransformer.transformToDTO(studentOptionalProgramHistoryRepository.findByStudentID(studentID));
        histList.forEach(sP -> {
            OptionalProgram gradOptionalProgram = webClient.get()
                    .uri(String.format(constants.getGradOptionalProgramNameUrl(), sP.getOptionalProgramID()))
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(OptionalProgram.class)
                    .block();
            if (gradOptionalProgram != null) {
                sP.setOptionalProgramName(gradOptionalProgram.getOptionalProgramName());
                sP.setOptionalProgramCode(gradOptionalProgram.getOptProgramCode());
                sP.setProgramCode(gradOptionalProgram.getGraduationProgramCode());
            }

            Optional<HistoryActivityCodeEntity> entOpt = historyActivityRepository.findById(sP.getActivityCode());
            entOpt.ifPresent(historyActivityCodeEntity -> sP.setActivityCodeDescription(historyActivityCodeEntity.getDescription()));
        });
        return histList;
    }

    public GraduationStudentRecordHistory getStudentHistoryByID(UUID historyID) {
        return graduationStudentRecordHistoryTransformer.transformToDTO(graduationStudentRecordHistoryRepository.findById(historyID));
    }

    public StudentOptionalProgramHistory getStudentOptionalProgramHistoryByID(UUID historyID, String accessToken) {
        StudentOptionalProgramHistory obj = studentOptionalProgramHistoryTransformer.transformToDTO(studentOptionalProgramHistoryRepository.findById(historyID));
        if (obj.getOptionalProgramID() != null) {
            OptionalProgram gradOptionalProgram = webClient.get()
                    .uri(String.format(constants.getGradOptionalProgramNameUrl(), obj.getOptionalProgramID()))
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(OptionalProgram.class)
                    .block();
            if (gradOptionalProgram != null) {
                obj.setOptionalProgramName(gradOptionalProgram.getOptionalProgramName());
                obj.setOptionalProgramCode(gradOptionalProgram.getOptProgramCode());
                obj.setProgramCode(gradOptionalProgram.getGraduationProgramCode());
            }
        }
        return obj;
    }

    public Page<GraduationStudentRecordHistoryEntity> getStudentHistoryByBatchID(Long batchId, Integer pageNumber, Integer pageSize, String accessToken) {
        Pageable paging = PageRequest.of(pageNumber, pageSize);
        Page<GraduationStudentRecordHistoryEntity> pagedDate = graduationStudentRecordHistoryRepository.findByBatchId(batchId, paging);
        List<GraduationStudentRecordHistoryEntity> list = pagedDate.getContent();
        list.forEach(ent -> {
            Student stuData = webClient.get().uri(String.format(constants.getPenStudentApiByStudentIdUrl(), ent.getStudentID()))
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve().bodyToMono(Student.class).block();
            if (stuData != null) {
                ent.setPen(stuData.getPen());
                ent.setLegalFirstName(stuData.getLegalFirstName());
                ent.setLegalMiddleNames(stuData.getLegalMiddleNames());
                ent.setLegalLastName(stuData.getLegalLastName());
            }
            ent.setStudentGradData(null);
        });
        return pagedDate;
    }

    @Transactional
    public Integer updateStudentRecordHistoryDistributionRun(Long batchId, String updateUser, String activityCode, List<UUID> studentGuids) {
        Integer historyRecordsCreated = 0;
        if(studentGuids != null && !studentGuids.isEmpty()) {
            int partitionSize = 999;
            List<List<UUID>> partitions = new LinkedList<>();
            for (int i = 0; i < studentGuids.size(); i += partitionSize) {
                partitions.add(studentGuids.subList(i, Math.min(i + partitionSize, studentGuids.size())));
            }
            logger.debug("Update Student Record History partitions length {}", partitions.size());
            for (int i = 0; i < partitions.size(); i++) {
                List<UUID> subList = partitions.get(i);
                Integer studentRecordsCreatedPartition = graduationStatusRepository.updateGraduationStudentRecordEntitiesBatchIdWhereStudentIDsIn(batchId, subList);
                Integer historyRecordsCreatedPartition = graduationStudentRecordHistoryRepository.insertGraduationStudentRecordHistoryByBatchIdAndStudentIDs(batchId, subList, activityCode, updateUser);
                assert Objects.equals(studentRecordsCreatedPartition, historyRecordsCreatedPartition);
                historyRecordsCreated += historyRecordsCreatedPartition;
            }
        } else if(StringUtils.equalsIgnoreCase(activityCode, "USERSTUDARC")) {
            historyRecordsCreated = graduationStudentRecordHistoryRepository.insertGraduationStudentRecordHistoryByBatchId(batchId, activityCode, updateUser);
        } else if(StringUtils.isBlank(activityCode) || StringUtils.equalsIgnoreCase(activityCode, "null")) {
            historyRecordsCreated = graduationStudentRecordHistoryRepository.updateGradStudentUpdateUser(batchId, updateUser);
        } else {
            historyRecordsCreated = graduationStudentRecordHistoryRepository.updateGradStudentUpdateUser(batchId, activityCode, updateUser);
        }
        return historyRecordsCreated;
    }

}
