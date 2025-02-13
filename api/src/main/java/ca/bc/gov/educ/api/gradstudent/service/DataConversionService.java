package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.FieldName;
import ca.bc.gov.educ.api.gradstudent.constant.TraxEventType;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCareerProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentCareerProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentOptionalProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Initial Student Loads
 * & Ongoing Updates from TRAX to GRAD
 */
@Slf4j
@Service
public class DataConversionService extends GradBaseService {

    public static final String NULL_VALUE = "NULL"; // NULL String => Nullify (set to NULL)
    private static final String CREATE_USER = "createUser";
    private static final String CREATE_DATE = "createDate";
    public static final String DEFAULT_CREATED_BY = "DATA_CONV";
    public static final String DEFAULT_UPDATED_BY = "DATA_CONV";
    private static final String DATA_CONVERSION_HISTORY_ACTIVITY_CODE = "DATACONVERT";
    private static final String ADD_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXADD";
    private static final String UPDATE_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXUPDATE";
    private static final String DELETE_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXDELETE";
    private static final String ONGOING_UPDATE_FIELD_STR = " ==> {} for old value={}";

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
    final StudentNoteRepository studentNoteRepository;

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
                                 HistoryService historyService, StudentNoteRepository studentNoteRepository, GraduationStatusService graduationStatusService, GradValidation validation, EducGradStudentApiConstants constants) {
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
        this.studentNoteRepository = studentNoteRepository;
        this.graduationStatusService = graduationStatusService;
        this.validation = validation;
        this.constants = constants;
    }

    /**
     * Create or Update a GraduationStudentRecord
     */
    @Transactional
    public GraduationStudentRecord saveGraduationStudentRecord(UUID studentID, GraduationStudentRecord graduationStatus, boolean ongoingUpdate) {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        GraduationStudentRecordEntity sourceObject = graduationStatusTransformer.transformToEntity(graduationStatus);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            gradEntity = handleExistingGraduationStatus(sourceObject, gradEntity, graduationStatus.getPen(), ongoingUpdate);
            return graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(gradEntity);
        } else {
            sourceObject = handleNewGraduationStatus(sourceObject, graduationStatus.getPen(), ongoingUpdate);
            return graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(sourceObject);
        }
    }

    /**
     * Update Graduation Status at field level for Ongoing Updates
     */
    @Transactional
    public GraduationStudentRecord updateGraduationStatusByFields(OngoingUpdateRequestDTO requestDTO, String accessToken) {
        UUID studentID = UUID.fromString(requestDTO.getStudentID());
        TraxEventType eventType = requestDTO.getEventType();
        log.info("Perform ongoing update event [{}] for studentID = {}", eventType, studentID);
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            populateOngoingUpdateFields(requestDTO.getUpdateFields(), gradEntity, accessToken);
            gradEntity.setUpdateDate(null);
            gradEntity.setUpdateUser(null);
            validateStudentStatusAndResetBatchFlags(gradEntity);
            if (isBatchFlagUpdatesOnly(eventType)) {
                gradEntity = saveBatchFlagsWithAuditHistory(gradEntity.getStudentID(), gradEntity.getRecalculateGradStatus(),
                        gradEntity.getRecalculateProjectedGrad(), UPDATE_ONGOING_HISTORY_ACTIVITY_CODE);
            } else {
                gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
                historyService.createStudentHistory(gradEntity, UPDATE_ONGOING_HISTORY_ACTIVITY_CODE);
            }
            if (constants.isStudentGuidPenXrefEnabled() && StringUtils.isNotBlank(requestDTO.getPen())) {
                saveStudentGuidPenXref(gradEntity.getStudentID(), requestDTO.getPen());
            }
            return graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(gradEntity);
        }
        return null;
    }

    private boolean isBatchFlagUpdatesOnly(TraxEventType eventType) {
        return TraxEventType.NEWSTUDENT != eventType && TraxEventType.UPD_GRAD != eventType;
    }

    @Transactional
    @Retry(name = "generalpostcall")
    public StudentOptionalProgram saveStudentOptionalProgram(StudentOptionalProgramRequestDTO studentOptionalProgramReq, String accessToken) {
        if (studentOptionalProgramReq.getOptionalProgramID() == null) {
            Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentOptionalProgramReq.getStudentID());
            if (gradStatusOptional.isPresent()) {
                String currentGradProgramCode = gradStatusOptional.get().getProgram();
                // GRAD2-2412: concurrency issue as grad program might be changed by another event by a millisecond.
                //  => if the requested grad program is different from the current grad program, then use the existing one.
                if (!StringUtils.equalsIgnoreCase(studentOptionalProgramReq.getMainProgramCode(), currentGradProgramCode)) {
                    studentOptionalProgramReq.setMainProgramCode(currentGradProgramCode);
                }
            }
            OptionalProgram gradOptionalProgram = graduationStatusService.getOptionalProgram(studentOptionalProgramReq.getMainProgramCode(), studentOptionalProgramReq.getOptionalProgramCode(), accessToken);
            if (gradOptionalProgram == null) {
                return null;
            }
            studentOptionalProgramReq.setOptionalProgramID(gradOptionalProgram.getOptionalProgramID());
        }
        Optional<StudentOptionalProgramEntity> gradStudentOptionalOptional = gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentOptionalProgramReq.getStudentID(), studentOptionalProgramReq.getOptionalProgramID());

        if (gradStudentOptionalOptional.isPresent()) {  // if it exists, just touch the entity in data conversion
            StudentOptionalProgramEntity gradEntity = gradStudentOptionalOptional.get();
            gradEntity = handleExistingOptionalProgram(studentOptionalProgramReq, gradEntity);
            return gradStudentOptionalProgramTransformer.transformToDTO(gradEntity);
        } else {
            StudentOptionalProgramEntity sourceObject = new StudentOptionalProgramEntity();
            sourceObject.setId(studentOptionalProgramReq.getId());
            sourceObject.setStudentID(studentOptionalProgramReq.getStudentID());
            sourceObject.setOptionalProgramID(studentOptionalProgramReq.getOptionalProgramID());
            sourceObject = handleNewOptionalProgram(studentOptionalProgramReq, sourceObject);
            return gradStudentOptionalProgramTransformer.transformToDTO(sourceObject);
        }
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

    /**
     * Update Batch Flags in Graduation Status for Ongoing Updates
     */
    private GraduationStudentRecordEntity saveBatchFlagsWithAuditHistory(UUID studentID, String recalculateGradStatus, String recalculateProjectedGrad, String historyActivityCode) {
        graduationStatusRepository.updateGradStudentRecalculationAllFlags(studentID, recalculateGradStatus, recalculateProjectedGrad);
        gradStudentRecordHistoryRepository.insertGraduationStudentRecordHistoryByStudentId(studentID, historyActivityCode);
        return graduationStatusRepository.findByStudentID(studentID);
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
        // student_record_note
        studentNoteRepository.deleteByStudentID(studentID);
        // student_career_program
        gradStudentCareerProgramRepository.deleteByStudentID(studentID);
        // student_optional_program_history
        gradStudentOptionalProgramHistoryRepository.deleteByStudentID(studentID);
        // student_optional_program
        gradStudentOptionalProgramRepository.deleteByStudentID(studentID);
        // graduation_student_record_history
        gradStudentRecordHistoryRepository.deleteByStudentID(studentID);
    }

    private GraduationStudentRecordEntity handleExistingGraduationStatus(GraduationStudentRecordEntity sourceObject, GraduationStudentRecordEntity targetObject, String pen, boolean ongoingUpdate) {
        BeanUtils.copyProperties(sourceObject, targetObject, CREATE_USER, CREATE_DATE);
        targetObject.setUpdateDate(null);
        targetObject.setUpdateUser(null);
        targetObject = graduationStatusRepository.saveAndFlush(targetObject);
        if (ongoingUpdate) {
            historyService.createStudentHistory(targetObject, UPDATE_ONGOING_HISTORY_ACTIVITY_CODE);
        }
        if (constants.isStudentGuidPenXrefEnabled() && StringUtils.isNotBlank(pen)) {
            saveStudentGuidPenXref(targetObject.getStudentID(), pen);
        }
        return targetObject;
    }

    private GraduationStudentRecordEntity handleNewGraduationStatus(GraduationStudentRecordEntity newObject, String pen, boolean ongoingUpdate) {
        newObject = graduationStatusRepository.saveAndFlush(newObject);
        if (ongoingUpdate) {
            historyService.createStudentHistory(newObject, ADD_ONGOING_HISTORY_ACTIVITY_CODE);
        } else {
            historyService.createStudentHistory(newObject, DATA_CONVERSION_HISTORY_ACTIVITY_CODE);
        }
        if (constants.isStudentGuidPenXrefEnabled() && StringUtils.isNotBlank(pen)) {
            saveStudentGuidPenXref(newObject.getStudentID(), pen);
        }
        return newObject;
    }

    private void populateOngoingUpdateFields(List<OngoingUpdateFieldDTO> fields, GraduationStudentRecordEntity targetObject, String accessToken) {
        fields.forEach(f -> {
            if (f.getName() == FieldName.GRAD_PROGRAM) {
                String newProgram = getStringValue(f.getValue());
                String currentProgram = targetObject.getProgram();
                handleStudentAchievements(currentProgram, newProgram, targetObject, accessToken);
                resetAdultStartDate(currentProgram, newProgram, targetObject);
            } else if (f.getName() == FieldName.STUDENT_STATUS) {
                String newStatus = getStringValue(f.getValue());
                if ("MER".equals(newStatus)) {
                    targetObject.setStudentGradData(null);
                    targetObject.setStudentProjectedGradData(null);
                    targetObject.setRecalculateGradStatus(null);
                    targetObject.setRecalculateProjectedGrad(null);
                    log.info(" {} ==> {}: Delete Student Achievements.", targetObject.getStudentStatus(), newStatus);
                    graduationStatusService.deleteStudentAchievements(targetObject.getStudentID(), accessToken);
                }
            }
            populate(f, targetObject);
        });
    }

    private void handleStudentAchievements(String currentProgram, String newProgram, GraduationStudentRecordEntity targetObject, String accessToken) {
        if (!StringUtils.equalsIgnoreCase(currentProgram, newProgram)) {
            if("SCCP".equalsIgnoreCase(currentProgram)) {
                log.info(" {} ==> {}: Archive Student Achievements and SLP_DATE is set to null.", currentProgram, newProgram);
                targetObject.setProgramCompletionDate(null);
                graduationStatusService.archiveStudentAchievements(targetObject.getStudentID(),accessToken);
            } else {
                log.info(" {} ==> {}: Delete Student Achievements.", currentProgram, newProgram);
                graduationStatusService.deleteStudentAchievements(targetObject.getStudentID(), accessToken);
            }
        }
    }

    private void resetAdultStartDate(String currentProgram, String newProgram, GraduationStudentRecordEntity targetObject) {
        // Only when 1950 adult program is changed to another, reset adultStartDate to null
        if (!StringUtils.equalsIgnoreCase(currentProgram, newProgram) && "1950".equalsIgnoreCase(currentProgram)) {
            targetObject.setAdultStartDate(null);
        }
    }

    private void populate(OngoingUpdateFieldDTO field, GraduationStudentRecordEntity targetObject) {
        switch (field.getName()) {
            case SCHOOL_OF_RECORD -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, targetObject.getSchoolOfRecord());
                targetObject.setSchoolOfRecord(getStringValue(field.getValue()));
            }
            case GRAD_PROGRAM -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, targetObject.getProgram());
                targetObject.setProgram(getStringValue(field.getValue()));
            }
            case ADULT_START_DATE -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, EducGradStudentApiUtils.formatDate(targetObject.getAdultStartDate(), EducGradStudentApiConstants.DEFAULT_DATE_FORMAT));
                // date format: yyyy-MM-dd
                Date adultStartDate = EducGradStudentApiUtils.parseDate((String) field.getValue());
                targetObject.setAdultStartDate(adultStartDate);
            }
            case SLP_DATE -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, EducGradStudentApiUtils.formatDate(targetObject.getProgramCompletionDate(), EducGradStudentApiConstants.PROGRAM_COMPLETION_DATE_FORMAT));
                // date format: yyyy/MM
                Date programCompletionDate = EducGradStudentApiUtils.parsingProgramCompletionDate((String) field.getValue());
                targetObject.setProgramCompletionDate(programCompletionDate);
            }
            case STUDENT_GRADE -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, targetObject.getStudentGrade());
                targetObject.setStudentGrade(getStringValue(field.getValue()));
            }
            case CITIZENSHIP -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, targetObject.getStudentCitizenship());
                targetObject.setStudentCitizenship(getStringValue(field.getValue()));
            }
            case STUDENT_STATUS -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, targetObject.getStudentStatus());
                targetObject.setStudentStatus(getStringValue(field.getValue()));
            }
            case RECALC_GRAD_ALG -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, targetObject.getRecalculateGradStatus());
                targetObject.setRecalculateGradStatus(getStringValue(field.getValue()));
            }
            case RECALC_TVR -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, targetObject.getRecalculateProjectedGrad());
                targetObject.setRecalculateProjectedGrad(getStringValue(field.getValue()));
            }
        }
    }

    private String getStringValue(Object value) {
        if (value instanceof String str) {
            return NULL_VALUE.equalsIgnoreCase(str) ? null : str;
        }
        return null;
    }

    private StudentOptionalProgramEntity handleExistingOptionalProgram(StudentOptionalProgramRequestDTO studentOptionalProgramReq, StudentOptionalProgramEntity gradEntity) {
        if (studentOptionalProgramReq.getStudentOptionalProgramData() != null) {
            gradEntity.setStudentOptionalProgramData(studentOptionalProgramReq.getStudentOptionalProgramData());
        }
        if (studentOptionalProgramReq.getOptionalProgramCompletionDate() != null) {
            if (studentOptionalProgramReq.getOptionalProgramCompletionDate().length() > 7) {
                gradEntity.setOptionalProgramCompletionDate(java.sql.Date.valueOf(studentOptionalProgramReq.getOptionalProgramCompletionDate()));
            } else {
                gradEntity.setOptionalProgramCompletionDate(EducGradStudentApiUtils.parsingProgramCompletionDate(studentOptionalProgramReq.getOptionalProgramCompletionDate()));
            }
        }
        gradEntity.setUpdateDate(null);
        gradEntity.setUpdateUser(null);
        gradEntity = gradStudentOptionalProgramRepository.save(gradEntity);
        return gradEntity;
    }

    private StudentOptionalProgramEntity handleNewOptionalProgram(StudentOptionalProgramRequestDTO studentOptionalProgramReq, StudentOptionalProgramEntity sourceObject) {
        if (studentOptionalProgramReq.getOptionalProgramCompletionDate() != null) {
            if (studentOptionalProgramReq.getOptionalProgramCompletionDate().length() > 7) {
                sourceObject.setOptionalProgramCompletionDate(java.sql.Date.valueOf(studentOptionalProgramReq.getOptionalProgramCompletionDate()));
            } else {
                sourceObject.setOptionalProgramCompletionDate(EducGradStudentApiUtils.parsingProgramCompletionDate(studentOptionalProgramReq.getOptionalProgramCompletionDate()));
            }
        }
        if (sourceObject.getId() == null) {
            sourceObject.setId(UUID.randomUUID());
        }
        sourceObject = gradStudentOptionalProgramRepository.save(sourceObject);
        historyService.createStudentOptionalProgramHistory(sourceObject, DATA_CONVERSION_HISTORY_ACTIVITY_CODE);
        return sourceObject;
    }


}
