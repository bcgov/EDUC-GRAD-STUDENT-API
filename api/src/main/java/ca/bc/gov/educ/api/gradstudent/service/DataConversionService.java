package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentCareerProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentOptionalProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Initial Student Loads
 * & Ongoing Updates from TRAX to GRAD
 */
@Slf4j
@Service
public class DataConversionService {

    private static final String CREATE_USER = "createUser";
    private static final String CREATE_DATE = "createDate";
    public static final String DEFAULT_CREATED_BY = "DATA_CONV";
    public static final String DEFAULT_UPDATED_BY = "DATA_CONV";
    private static final String DATA_CONVERSION_HISTORY_ACTIVITY_CODE = "DATACONVERT";
    private static final String ADD_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXADD";
    private static final String UPDATE_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXUPDATE";
    private static final String DELETE_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXDELETE";
    public static final String STUDENT_STATUS_MERGED = "MER";

    final WebClient webClient;
    final GraduationStudentRecordRepository graduationStatusRepository;
    final GraduationStatusTransformer graduationStatusTransformer;
    final StudentOptionalProgramRepository gradStudentOptionalProgramRepository;
    final GradStudentOptionalProgramTransformer gradStudentOptionalProgramTransformer;
    final StudentCareerProgramRepository gradStudentCareerProgramRepository;
    final GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer;
    final StudentOptionalProgramHistoryRepository gradStudentOptionalProgramHistoryRepository;
    final GraduationStudentRecordHistoryRepository gradStudentRecordHistoryRepository;
    final HistoryService historyService;

    final GraduationStatusService graduationStatusService;
    final GradValidation validation;
    final EducGradStudentApiConstants constants;

    @Autowired
    public DataConversionService(WebClient webClient,
                                 GraduationStudentRecordRepository graduationStatusRepository,
                                 GraduationStatusTransformer graduationStatusTransformer,
                                 StudentOptionalProgramRepository gradStudentOptionalProgramRepository, GradStudentOptionalProgramTransformer gradStudentOptionalProgramTransformer,
                                 StudentCareerProgramRepository gradStudentCareerProgramRepository, GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer,
                                 StudentOptionalProgramHistoryRepository gradStudentOptionalProgramHistoryRepository,
                                 GraduationStudentRecordHistoryRepository gradStudentRecordHistoryRepository,
                                 HistoryService historyService, GraduationStatusService graduationStatusService, GradValidation validation, EducGradStudentApiConstants constants) {
        this.webClient = webClient;
        this.graduationStatusRepository = graduationStatusRepository;
        this.graduationStatusTransformer = graduationStatusTransformer;
        this.gradStudentOptionalProgramRepository = gradStudentOptionalProgramRepository;
        this.gradStudentOptionalProgramTransformer = gradStudentOptionalProgramTransformer;
        this.gradStudentCareerProgramRepository = gradStudentCareerProgramRepository;
        this.gradStudentCareerProgramTransformer = gradStudentCareerProgramTransformer;
        this.gradStudentOptionalProgramHistoryRepository = gradStudentOptionalProgramHistoryRepository;
        this.gradStudentRecordHistoryRepository = gradStudentRecordHistoryRepository;
        this.historyService = historyService;
        this.graduationStatusService = graduationStatusService;
        this.validation = validation;
        this.constants = constants;
    }

    /**
     * Create or Update a GraduationStudentRecord
     *
     * @param studentID
     * @param graduationStatus
     * @return
     */
    @Transactional
    public GraduationStudentRecord saveGraduationStudentRecord(UUID studentID, GraduationStudentRecord graduationStatus, boolean ongoingUpdate, String accessToken) {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        GraduationStudentRecordEntity sourceObject = graduationStatusTransformer.transformToEntity(graduationStatus);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();

            if (!sourceObject.getProgram().equalsIgnoreCase(gradEntity.getProgram())) {
                if(gradEntity.getProgram().equalsIgnoreCase("SCCP")) {
                    sourceObject.setProgramCompletionDate(null);
                    graduationStatusService.archiveStudentAchievements(sourceObject.getStudentID(),accessToken);
                } else {
                    graduationStatusService.deleteStudentAchievements(sourceObject.getStudentID(), accessToken);
                }
            }

            BeanUtils.copyProperties(sourceObject, gradEntity,  CREATE_USER, CREATE_DATE);
            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            if (ongoingUpdate) {
                historyService.createStudentHistory(sourceObject, UPDATE_ONGOING_HISTORY_ACTIVITY_CODE);
            }
            if (constants.isStudentGuidPenXrefEnabled() && StringUtils.isNotBlank(graduationStatus.getPen())) {
                saveStudentGuidPenXref(gradEntity.getStudentID(), graduationStatus.getPen());
            }
            return graduationStatusTransformer.transformToDTO(gradEntity);
        } else {
            sourceObject = graduationStatusRepository.saveAndFlush(sourceObject);
            if (ongoingUpdate) {
                historyService.createStudentHistory(sourceObject, ADD_ONGOING_HISTORY_ACTIVITY_CODE);
            } else {
                historyService.createStudentHistory(sourceObject, DATA_CONVERSION_HISTORY_ACTIVITY_CODE);
            }
            if (constants.isStudentGuidPenXrefEnabled()) {
                saveStudentGuidPenXref(sourceObject.getStudentID(), graduationStatus.getPen());
            }
            return graduationStatusTransformer.transformToDTO(sourceObject);
        }
    }

    @Transactional
    @Retry(name = "generalpostcall")
    public StudentOptionalProgram saveStudentOptionalProgram(StudentOptionalProgramRequestDTO studentOptionalProgramReq, String accessToken) {
        if (studentOptionalProgramReq.getOptionalProgramID() == null) {
            OptionalProgram gradOptionalProgram = getOptionalProgram(studentOptionalProgramReq.getMainProgramCode(), studentOptionalProgramReq.getOptionalProgramCode(), accessToken);
            if (gradOptionalProgram == null) {
                return null;
            }
            studentOptionalProgramReq.setOptionalProgramID(gradOptionalProgram.getOptionalProgramID());
        }
        Optional<StudentOptionalProgramEntity> gradStudentOptionalOptional = gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentOptionalProgramReq.getStudentID(), studentOptionalProgramReq.getOptionalProgramID());

        if (gradStudentOptionalOptional.isPresent()) {  // if it exists, just touch the entity in data conversion
            StudentOptionalProgramEntity gradEntity = gradStudentOptionalOptional.get();
            if (studentOptionalProgramReq.getStudentOptionalProgramData() != null) {
                gradEntity.setStudentOptionalProgramData(studentOptionalProgramReq.getStudentOptionalProgramData());
            }
            if (studentOptionalProgramReq.getOptionalProgramCompletionDate() != null) {
                if (studentOptionalProgramReq.getOptionalProgramCompletionDate().length() > 7) {
                    gradEntity.setOptionalProgramCompletionDate(Date.valueOf(studentOptionalProgramReq.getOptionalProgramCompletionDate()));
                } else {
                    gradEntity.setOptionalProgramCompletionDate(EducGradStudentApiUtils.parsingProgramCompletionDate(studentOptionalProgramReq.getOptionalProgramCompletionDate()));
                }
            }
            gradEntity.setUpdateDate(null);
            gradEntity.setUpdateUser(null);
            gradEntity = gradStudentOptionalProgramRepository.save(gradEntity);
            return gradStudentOptionalProgramTransformer.transformToDTO(gradEntity);
        } else {
            StudentOptionalProgramEntity sourceObject = new StudentOptionalProgramEntity();
            sourceObject.setId(studentOptionalProgramReq.getId());
            sourceObject.setStudentID(studentOptionalProgramReq.getStudentID());
            sourceObject.setOptionalProgramID(studentOptionalProgramReq.getOptionalProgramID());
            if (studentOptionalProgramReq.getOptionalProgramCompletionDate() != null) {
                if (studentOptionalProgramReq.getOptionalProgramCompletionDate().length() > 7) {
                    sourceObject.setOptionalProgramCompletionDate(Date.valueOf(studentOptionalProgramReq.getOptionalProgramCompletionDate()));
                } else {
                    sourceObject.setOptionalProgramCompletionDate(EducGradStudentApiUtils.parsingProgramCompletionDate(studentOptionalProgramReq.getOptionalProgramCompletionDate()));
                }
            }
            if (sourceObject.getId() == null) {
                sourceObject.setId(UUID.randomUUID());
            }
            sourceObject = gradStudentOptionalProgramRepository.save(sourceObject);
            historyService.createStudentOptionalProgramHistory(sourceObject, DATA_CONVERSION_HISTORY_ACTIVITY_CODE);
            return gradStudentOptionalProgramTransformer.transformToDTO(sourceObject);
        }
    }

    private OptionalProgram getOptionalProgram(String mainProgramCode, String optionalProgramCode, String accessToken) {
        OptionalProgram optionalProgram = null;
        try {
            optionalProgram = webClient.get()
                    .uri(String.format(constants.getGradOptionalProgramDetailsUrl(), mainProgramCode, optionalProgramCode))
                    .headers(h -> {
                        h.setBearerAuth(accessToken);
                        h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                    })
                    .retrieve()
                    .bodyToMono(OptionalProgram.class)
                    .block();
        } catch (Exception e) {
            log.error("Program API is failed to find an optional program: [{}] / [{}]", mainProgramCode, optionalProgramCode);
        }
        return optionalProgram;
    }

    @Transactional
    public StudentCareerProgram saveStudentCareerProgram(StudentCareerProgram studentCareerProgram) {
        Optional<StudentCareerProgramEntity> studentCareerOptional = gradStudentCareerProgramRepository.findByStudentIDAndCareerProgramCode(studentCareerProgram.getStudentID(), studentCareerProgram.getCareerProgramCode());
        if (studentCareerOptional.isPresent()) {
            StudentCareerProgramEntity gradEntity = studentCareerOptional.get();
            gradEntity.setUpdateDate(null);
            gradEntity.setUpdateUser(null);
            gradEntity = gradStudentCareerProgramRepository.save(gradEntity);
            return gradStudentCareerProgramTransformer.transformToDTO(gradEntity);
        } else {
            StudentCareerProgramEntity sourceObject = new StudentCareerProgramEntity();
            sourceObject.setId(studentCareerProgram.getId());
            sourceObject.setStudentID(studentCareerProgram.getStudentID());
            sourceObject.setCareerProgramCode(studentCareerProgram.getCareerProgramCode());
            if (sourceObject.getId() == null) {
                sourceObject.setId(UUID.randomUUID());
            }
            sourceObject = gradStudentCareerProgramRepository.save(sourceObject);
            return gradStudentCareerProgramTransformer.transformToDTO(sourceObject);
        }
    }

    @Transactional
    public void deleteStudentOptionalProgram(UUID optionalProgramID, UUID studentID) {
        Optional<StudentOptionalProgramEntity> optional = gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentID, optionalProgramID);
        if (optional.isPresent()) {
            StudentOptionalProgramEntity entity = optional.get();
            historyService.createStudentOptionalProgramHistory(entity, DELETE_ONGOING_HISTORY_ACTIVITY_CODE);
            gradStudentOptionalProgramRepository.delete(entity);
        }
    }

    @Transactional
    public void deleteStudentCareerProgram(String careerProgramCode, UUID studentID) {
        Optional<StudentCareerProgramEntity> optional = gradStudentCareerProgramRepository.findByStudentIDAndCareerProgramCode(studentID, careerProgramCode);
        if (optional.isPresent()) {
            StudentCareerProgramEntity entity = optional.get();
            gradStudentCareerProgramRepository.delete(entity);
        }
    }

    private void saveStudentGuidPenXref(UUID studentId, String pen) {
        if (graduationStatusRepository.countStudentGuidPenXrefRecord(studentId) > 0) {
            graduationStatusRepository.updateStudentGuidPenXrefRecord(studentId, pen, DEFAULT_UPDATED_BY, LocalDateTime.now());
        } else {
            graduationStatusRepository.createStudentGuidPenXrefRecord(studentId, pen, DEFAULT_CREATED_BY, LocalDateTime.now());
        }
    }

    @Transactional
    public void deleteGraduationStatus(UUID studentID) {
        // graduation_student_record
        if (graduationStatusRepository.existsById(studentID)) {
            graduationStatusRepository.deleteById(studentID);
        }
    }

    @Transactional
    public void deleteAllDependencies(UUID studentID) {
        // student_career_program
        gradStudentCareerProgramRepository.deleteByStudentID(studentID);
        // student_optional_program_history
        gradStudentOptionalProgramHistoryRepository.deleteByStudentID(studentID);
        // student_optional_program
        gradStudentOptionalProgramRepository.deleteByStudentID(studentID);
        // graduation_student_record_history
        gradStudentRecordHistoryRepository.deleteByStudentID(studentID);
    }

}
