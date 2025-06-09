package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.constant.FieldName;
import ca.bc.gov.educ.api.gradstudent.constant.FieldType;
import ca.bc.gov.educ.api.gradstudent.constant.TraxEventType;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.assessment.v1.StudentForAssessmentUpdate;
import ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1.DemographicStudent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCareerProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentCareerProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentOptionalProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.util.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.nimbusds.jose.util.Pair;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.*;

import static ca.bc.gov.educ.api.gradstudent.model.dc.EventOutcome.SCHOOL_OF_RECORD_UPDATED;
import static ca.bc.gov.educ.api.gradstudent.model.dc.EventType.UPDATE_SCHOOL_OF_RECORD;

/**
 * Initial Student Loads
 * & Ongoing Updates from TRAX to GRAD
 */
@Slf4j
@Service
public class DataConversionService extends GradBaseService {

    private static final String CREATE_USER = "createUser";
    private static final String CREATE_DATE = "createDate";
    public static final String DEFAULT_CREATED_BY = "DATA_CONV";
    public static final String DEFAULT_UPDATED_BY = "DATA_CONV";
    private static final String DATA_CONVERSION_HISTORY_ACTIVITY_CODE = "DATACONVERT";
    private static final String ADD_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXADD";
    private static final String UPDATE_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXUPDATE";
    private static final String DELETE_ONGOING_HISTORY_ACTIVITY_CODE = "TRAXDELETE";
    private static final String ONGOING_UPDATE_FIELD_STR = " {} for old value={}";

    private static final String UPDATE_FIELD_STR = " ==> Update Field [{}]={}";

    final WebClient studentApiClient;
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

    private final GradStatusEventRepository gradStatusEventRepository;

    @Autowired
    public DataConversionService(@Qualifier("studentApiClient") WebClient studentApiClient,
                                 GraduationStudentRecordRepository graduationStatusRepository,
                                 GraduationStatusTransformer graduationStatusTransformer,
                                 StudentOptionalProgramRepository gradStudentOptionalProgramRepository, GradStudentOptionalProgramTransformer gradStudentOptionalProgramTransformer,
                                 StudentCareerProgramRepository gradStudentCareerProgramRepository, GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer,
                                 StudentOptionalProgramHistoryRepository gradStudentOptionalProgramHistoryRepository,
                                 GraduationStudentRecordHistoryRepository gradStudentRecordHistoryRepository,
                                 HistoryService historyService, StudentNoteRepository studentNoteRepository, GraduationStatusService graduationStatusService, GradValidation validation, EducGradStudentApiConstants constants, GradStatusEventRepository gradStatusEventRepository) {
        this.studentApiClient = studentApiClient;
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
        this.gradStatusEventRepository = gradStatusEventRepository;
    }

    /**
     * Create or Update a GraduationStudentRecord
     */
    @Transactional
    public Pair<GraduationStudentRecord, GradStatusEvent> saveGraduationStudentRecord(UUID studentID, GraduationStudentRecord graduationStatus, boolean ongoingUpdate) throws JsonProcessingException {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        GraduationStudentRecordEntity sourceObject = graduationStatusTransformer.transformToEntity(graduationStatus);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            boolean isSchoolOfRecordUpdated = checkIfSchoolOfRecordIsUpdated(sourceObject, gradEntity);
            gradEntity = handleExistingGraduationStatus(sourceObject, gradEntity, graduationStatus.getPen(), ongoingUpdate);

            GradStatusEvent gradStatusEvent = null;
            if(isSchoolOfRecordUpdated) {
                var studentForUpdate = StudentForAssessmentUpdate
                        .builder()
                        .studentID(String.valueOf(studentID))
                        .schoolOfRecordID(String.valueOf(sourceObject.getSchoolOfRecordId()))
                        .vendorID(null)
                        .build();
                gradStatusEvent = EventUtil.createEvent(sourceObject.getCreateUser(),
                        sourceObject.getUpdateUser(), JsonUtil.getJsonStringFromObject(studentForUpdate), UPDATE_SCHOOL_OF_RECORD, SCHOOL_OF_RECORD_UPDATED);
                gradStatusEventRepository.save(gradStatusEvent);
            }
            var savedRecord = graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(gradEntity);
            return Pair.of(savedRecord, gradStatusEvent);
        } else {
            sourceObject = handleNewGraduationStatus(sourceObject, graduationStatus.getPen(), ongoingUpdate);
            var savedRecord = graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(sourceObject);
            return Pair.of(savedRecord, null);
        }
    }

    private boolean checkIfSchoolOfRecordIsUpdated(GraduationStudentRecordEntity updatedEntity, GraduationStudentRecordEntity existingEntity) {
        return existingEntity.getSchoolOfRecordId() != null
                && updatedEntity.getSchoolOfRecordId() != null
                && existingEntity.getSchoolOfRecordId() != updatedEntity.getSchoolOfRecordId()
                && (updatedEntity.getStudentStatus().equalsIgnoreCase("A") || updatedEntity.getStudentStatus().equalsIgnoreCase("T"));
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
            Map<FieldName, OngoingUpdateFieldDTO> updateFieldsMap = populateOngoingUpdateFields(requestDTO.getUpdateFields(), gradEntity, accessToken);
            validateStudentStatusAndResetBatchFlags(gradEntity, updateFieldsMap);
            gradEntity = saveUpdateFields(studentID, updateFieldsMap, getUsername());
            if (constants.isStudentGuidPenXrefEnabled() && StringUtils.isNotBlank(requestDTO.getPen())) {
                saveStudentGuidPenXref(studentID, requestDTO.getPen());
            }
            return graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(gradEntity);
        }
        return null;
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

    private Map<FieldName, OngoingUpdateFieldDTO> populateOngoingUpdateFields(List<OngoingUpdateFieldDTO> fields, GraduationStudentRecordEntity gradEntity, String accessToken) {
        Map<FieldName, OngoingUpdateFieldDTO> updateFieldsMap = new EnumMap<>(FieldName.class);
        fields.forEach(f -> {
            populate(f, updateFieldsMap, gradEntity);
            if (f.getName() == FieldName.GRAD_PROGRAM) {
                String newProgram = getStringValue(f.getValue());
                String currentProgram = gradEntity.getProgram();
                handleStudentAchievements(currentProgram, newProgram, gradEntity.getStudentID(), updateFieldsMap, accessToken);
                resetAdultStartDate(currentProgram, newProgram, updateFieldsMap);
            } else if (f.getName() == FieldName.STUDENT_STATUS) {
                String newStatus = getStringValue(f.getValue());
                if ("MER".equals(newStatus)) {
                    addUpdateFieldIntoMap(updateFieldsMap, FieldName.GRAD_ALG_CLOB, FieldType.STRING, NULL_VALUE);
                    addUpdateFieldIntoMap(updateFieldsMap, FieldName.TVR_CLOB, FieldType.STRING, NULL_VALUE);
                    addUpdateFieldIntoMap(updateFieldsMap, FieldName.RECALC_GRAD_ALG, FieldType.STRING, NULL_VALUE);
                    addUpdateFieldIntoMap(updateFieldsMap, FieldName.RECALC_TVR, FieldType.STRING, NULL_VALUE);
                    log.info(" {} ==> {}: Delete Student Achievements.", gradEntity.getStudentStatus(), newStatus);
                    graduationStatusService.deleteStudentAchievements(gradEntity.getStudentID(), accessToken);
                }
            }
        });
        return updateFieldsMap;
    }

    private GraduationStudentRecordEntity saveUpdateFields(UUID studentID, Map<FieldName, OngoingUpdateFieldDTO> fieldsMap, String updateUser) {
        fieldsMap.forEach((f, v) -> performUpdateField(studentID, v, updateUser));
        gradStudentRecordHistoryRepository.insertGraduationStudentRecordHistoryByStudentId(studentID, UPDATE_ONGOING_HISTORY_ACTIVITY_CODE);
        return graduationStatusRepository.findByStudentID(studentID);
    }

    private void handleStudentAchievements(String currentProgram, String newProgram, UUID studentID, Map<FieldName, OngoingUpdateFieldDTO> updateFieldsMap, String accessToken) {
        if (!StringUtils.equalsIgnoreCase(currentProgram, newProgram)) {
            if("SCCP".equalsIgnoreCase(currentProgram)) {
                log.info(" {} ==> {}: Archive Student Achievements and SLP_DATE is set to null.", currentProgram, newProgram);
                addUpdateFieldIntoMap(updateFieldsMap, FieldName.SLP_DATE, FieldType.STRING, NULL_VALUE);
                graduationStatusService.archiveStudentAchievements(studentID,accessToken);
            } else {
                log.info(" {} ==> {}: Delete Student Achievements.", currentProgram, newProgram);
                graduationStatusService.deleteStudentAchievements(studentID, accessToken);
            }
        }
    }

    private void resetAdultStartDate(String currentProgram, String newProgram, Map<FieldName, OngoingUpdateFieldDTO> updateFieldsMap) {
        // Only when 1950 adult program is changed to another, reset adultStartDate to null
        if (!StringUtils.equalsIgnoreCase(currentProgram, newProgram) && "1950".equalsIgnoreCase(currentProgram)) {
            addUpdateFieldIntoMap(updateFieldsMap, FieldName.ADULT_START_DATE, FieldType.DATE, NULL_VALUE);
        }
    }

    private void populate(OngoingUpdateFieldDTO field, Map<FieldName, OngoingUpdateFieldDTO> updateFieldsMap, GraduationStudentRecordEntity currentEntity) {
        switch (field.getName()) {
            case SCHOOL_OF_RECORD_ID -> {
                UUID newSchoolId = getGuidValue(field.getValue());
                if (!currentEntity.getSchoolOfRecordId().equals(newSchoolId)) {
                    log.info(ONGOING_UPDATE_FIELD_STR, field, currentEntity.getSchoolOfRecordId());
                    addUpdateFieldIntoMap(updateFieldsMap,field);
                }
            }
            case GRAD_PROGRAM -> {
                String newProgram = getStringValue(field.getValue());
                if (!StringUtils.equalsIgnoreCase(newProgram, currentEntity.getProgram())) {
                    log.info(ONGOING_UPDATE_FIELD_STR, field, currentEntity.getProgram());
                    addUpdateFieldIntoMap(updateFieldsMap,field);
                }
            }
            case ADULT_START_DATE -> {
                String newAdultStartDate = getStringValue(field.getValue());
                String adultStartDate = currentEntity.getAdultStartDate() != null? EducGradStudentApiUtils.formatDate(currentEntity.getAdultStartDate(), EducGradStudentApiConstants.DEFAULT_DATE_FORMAT) : null;
                if (!StringUtils.equalsIgnoreCase(newAdultStartDate, adultStartDate)) {
                    log.info(ONGOING_UPDATE_FIELD_STR, field, adultStartDate);
                    addUpdateFieldIntoMap(updateFieldsMap,field);
                }
            }
            case SLP_DATE -> {
                String newProgramCompletionDate = getStringValue(field.getValue());
                String programCompletionDate = currentEntity.getProgramCompletionDate() != null? EducGradStudentApiUtils.formatDate(currentEntity.getProgramCompletionDate(), EducGradStudentApiConstants.PROGRAM_COMPLETION_DATE_FORMAT) : null;
                if (!StringUtils.equalsIgnoreCase(newProgramCompletionDate, programCompletionDate)) {
                    log.info(ONGOING_UPDATE_FIELD_STR, field, programCompletionDate);
                    addUpdateFieldIntoMap(updateFieldsMap,field);
                }
            }
            case STUDENT_GRADE -> {
                String newStudentGrade = getStringValue(field.getValue());
                if (!StringUtils.equalsIgnoreCase(newStudentGrade, currentEntity.getStudentGrade())) {
                    log.info(ONGOING_UPDATE_FIELD_STR, field, currentEntity.getStudentGrade());
                    addUpdateFieldIntoMap(updateFieldsMap,field);
                }
            }
            case CITIZENSHIP -> {
                String newCitizenship = getStringValue(field.getValue());
                if (!StringUtils.equalsIgnoreCase(newCitizenship, currentEntity.getStudentCitizenship())) {
                    log.info(ONGOING_UPDATE_FIELD_STR, field, currentEntity.getStudentCitizenship());
                    addUpdateFieldIntoMap(updateFieldsMap,field);
                }
            }
            case STUDENT_STATUS -> {
                String newStudentStatus = getStringValue(field.getValue());
                if (!StringUtils.equalsIgnoreCase(newStudentStatus, currentEntity.getStudentStatus())) {
                    log.info(ONGOING_UPDATE_FIELD_STR, field, currentEntity.getStudentStatus());
                    addUpdateFieldIntoMap(updateFieldsMap,field);
                }
            }
            case RECALC_GRAD_ALG -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, currentEntity.getRecalculateGradStatus());
                addUpdateFieldIntoMap(updateFieldsMap,field);
            }
            case RECALC_TVR -> {
                log.info(ONGOING_UPDATE_FIELD_STR, field, currentEntity.getRecalculateProjectedGrad());
                addUpdateFieldIntoMap(updateFieldsMap,field);
            }
        }
    }

    private void performUpdateField(UUID studentID, OngoingUpdateFieldDTO field, String updateUser) {
        switch (field.getName()) {
            case SCHOOL_OF_RECORD_ID -> {
                UUID schoolId = getGuidValue(field.getValue());
                log.info(UPDATE_FIELD_STR, field.getName(), schoolId);
                graduationStatusRepository.updateSchoolOfRecordId(studentID, schoolId, updateUser, LocalDateTime.now());
            }
            case GRAD_PROGRAM -> {
                String program = getStringValue(field.getValue());
                log.info(UPDATE_FIELD_STR, field.getName(), program);
                graduationStatusRepository.updateGradProgram(studentID, program, updateUser, LocalDateTime.now());
            }
            case ADULT_START_DATE -> {
                String dateStr = getStringValue(field.getValue());
                if (dateStr != null) {
                    // date format: yyyy-MM-dd
                    Date adultStartDate = EducGradStudentApiUtils.parseDate(dateStr);
                    log.info(UPDATE_FIELD_STR, field.getName(), EducGradStudentApiUtils.formatDate(adultStartDate, EducGradStudentApiConstants.DEFAULT_DATE_FORMAT));
                    graduationStatusRepository.updateAdultStartDate(studentID, adultStartDate, updateUser, LocalDateTime.now());
                } else {
                    log.info(UPDATE_FIELD_STR, field.getName(), null);
                    graduationStatusRepository.updateAdultStartDate(studentID, null, updateUser, LocalDateTime.now());
                }
            }
            case SLP_DATE -> {
                String dateStr = getStringValue(field.getValue());
                if (dateStr != null) {
                    // date format: yyyy/MM
                    Date programCompletionDate = EducGradStudentApiUtils.parsingProgramCompletionDate(dateStr);
                    log.info(UPDATE_FIELD_STR, field.getName(), EducGradStudentApiUtils.formatDate(programCompletionDate, EducGradStudentApiConstants.PROGRAM_COMPLETION_DATE_FORMAT));
                    graduationStatusRepository.updateProgramCompletionDate(studentID, programCompletionDate, updateUser, LocalDateTime.now());
                } else {
                    log.info(UPDATE_FIELD_STR, field.getName(), null);
                    graduationStatusRepository.updateProgramCompletionDate(studentID, null, updateUser, LocalDateTime.now());
                }
            }
            case STUDENT_GRADE -> {
                String grade = getStringValue(field.getValue());
                log.info(UPDATE_FIELD_STR, field.getName(), grade);
                graduationStatusRepository.updateStudentGrade(studentID, grade, updateUser, LocalDateTime.now());
            }
            case CITIZENSHIP -> {
                String citizenship = getStringValue(field.getValue());
                log.info(UPDATE_FIELD_STR, field.getName(), citizenship);
                graduationStatusRepository.updateStudentCitizenship(studentID, citizenship, updateUser, LocalDateTime.now());
            }
            case STUDENT_STATUS -> {
                String studentStatus = getStringValue(field.getValue());
                log.info(UPDATE_FIELD_STR, field.getName(), studentStatus);
                graduationStatusRepository.updateStudentStatus(studentID, studentStatus, updateUser, LocalDateTime.now());
            }
            case GRAD_ALG_CLOB -> {
                String gradStatusClob = getStringValue(field.getValue());
                log.info(UPDATE_FIELD_STR, field.getName(), gradStatusClob);
                graduationStatusRepository.updateGradStatusClob(studentID, gradStatusClob, updateUser, LocalDateTime.now());
            }
            case TVR_CLOB -> {
                String projectedGradClob = getStringValue(field.getValue());
                log.info(UPDATE_FIELD_STR, field.getName(), projectedGradClob);
                graduationStatusRepository.updateProjectedGradClob(studentID, projectedGradClob, updateUser, LocalDateTime.now());
            }
            case RECALC_GRAD_ALG -> {
                String recalculateGradAlg = getStringValue(field.getValue());
                log.info(UPDATE_FIELD_STR, field.getName(), recalculateGradAlg);
                graduationStatusRepository.updateRecalculateGradStatusFlag(studentID, recalculateGradAlg, updateUser, LocalDateTime.now());
            }
            case RECALC_TVR -> {
                String recalculateTvrRun = getStringValue(field.getValue());
                log.info(UPDATE_FIELD_STR, field.getName(), recalculateTvrRun);
                graduationStatusRepository.updateRecalculateProjectedGradFlag(studentID, recalculateTvrRun, updateUser, LocalDateTime.now());
            }
        }
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

    @Override
    protected WebClient getWebClient() {
        return studentApiClient;
    }

    @Override
    protected EducGradStudentApiConstants getConstants() {
        return constants;
    }

}
