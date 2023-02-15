package ca.bc.gov.educ.api.gradstudent.service;

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
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

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
    final HistoryService historyService;
    final GradValidation validation;
    final EducGradStudentApiConstants constants;

    @Autowired
    public DataConversionService(WebClient webClient,
                                 GraduationStudentRecordRepository graduationStatusRepository,
                                 GraduationStatusTransformer graduationStatusTransformer,
                                 StudentOptionalProgramRepository gradStudentOptionalProgramRepository, GradStudentOptionalProgramTransformer gradStudentOptionalProgramTransformer,
                                 StudentCareerProgramRepository gradStudentCareerProgramRepository, GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer,
                                 HistoryService historyService, GradValidation validation, EducGradStudentApiConstants constants) {
        this.webClient = webClient;
        this.graduationStatusRepository = graduationStatusRepository;
        this.graduationStatusTransformer = graduationStatusTransformer;
        this.gradStudentOptionalProgramRepository = gradStudentOptionalProgramRepository;
        this.gradStudentOptionalProgramTransformer = gradStudentOptionalProgramTransformer;
        this.gradStudentCareerProgramRepository = gradStudentCareerProgramRepository;
        this.gradStudentCareerProgramTransformer = gradStudentCareerProgramTransformer;
        this.historyService = historyService;
        this.validation = validation;
        this.constants = constants;
    }

    /**
     * Create or Update a GraduationStudentRecord
     *
     * @param studentID
     * @param graduationStatus
     * @return
     * @throws JsonProcessingException
     */
    @Transactional
    public GraduationStudentRecord saveGraduationStudentRecord(UUID studentID, GraduationStudentRecord graduationStatus, boolean ongoingUpdate) {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        GraduationStudentRecordEntity sourceObject = graduationStatusTransformer.transformToEntity(graduationStatus);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            BeanUtils.copyProperties(sourceObject, gradEntity,  CREATE_USER, CREATE_DATE);
            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            if (ongoingUpdate) {
                historyService.createStudentHistory(sourceObject, UPDATE_ONGOING_HISTORY_ACTIVITY_CODE);
            }
            if (constants.isStudentGuidPenXrefEnabled()) {
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
    public StudentOptionalProgram saveStudentOptionalProgram(StudentOptionalProgramReq studentOptionalProgramReq, String accessToken) {
        OptionalProgram gradOptionalProgram = webClient.get()
                .uri(String.format(constants.getGradOptionalProgramDetailsUrl(), studentOptionalProgramReq.getMainProgramCode(),studentOptionalProgramReq.getOptionalProgramCode()))
                .headers(h -> {
                    h.setBearerAuth(accessToken);
                    h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID());
                })
                .retrieve()
                .bodyToMono(OptionalProgram.class)
                .block();
        if (gradOptionalProgram == null) {
            return null;
        }
        Optional<StudentOptionalProgramEntity> gradStudentOptionalOptional = gradStudentOptionalProgramRepository.findByStudentIDAndOptionalProgramID(studentOptionalProgramReq.getStudentID(), gradOptionalProgram.getOptionalProgramID());

        if (gradStudentOptionalOptional.isPresent()) {  // if it exists, just touch the entity in data conversion
            StudentOptionalProgramEntity gradEntity = gradStudentOptionalOptional.get();
            gradEntity.setUpdateDate(null);
            gradEntity.setUpdateUser(null);
            gradEntity = gradStudentOptionalProgramRepository.save(gradEntity);
            return gradStudentOptionalProgramTransformer.transformToDTO(gradEntity);
        } else {
            StudentOptionalProgramEntity sourceObject = new StudentOptionalProgramEntity();
            sourceObject.setId(studentOptionalProgramReq.getId());
            sourceObject.setStudentID(studentOptionalProgramReq.getStudentID());
            sourceObject.setOptionalProgramID(gradOptionalProgram.getOptionalProgramID());
            sourceObject.setOptionalProgramCompletionDate(studentOptionalProgramReq.getOptionalProgramCompletionDate() != null ? EducGradStudentApiUtils.parsingProgramCompletionDate(studentOptionalProgramReq.getOptionalProgramCompletionDate()) : null);
            if (sourceObject.getId() == null) {
                sourceObject.setId(UUID.randomUUID());
            }
            sourceObject = gradStudentOptionalProgramRepository.save(sourceObject);
            historyService.createStudentOptionalProgramHistory(sourceObject, DATA_CONVERSION_HISTORY_ACTIVITY_CODE);
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

}
