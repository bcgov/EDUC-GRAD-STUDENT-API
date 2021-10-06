package ca.bc.gov.educ.api.gradstudent.service;


import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.repository.GradStatusEventRepository;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradstudent.model.dto.GradProgram;
import ca.bc.gov.educ.api.gradstudent.model.dto.GradStudentUngradReasons;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.OptionalProgram;
import ca.bc.gov.educ.api.gradstudent.model.dto.School;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgramReq;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentStatus;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentUngradReason;
import ca.bc.gov.educ.api.gradstudent.model.dto.UngradReason;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramRepository;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentSpecialProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;

import static ca.bc.gov.educ.api.gradstudent.constant.EventStatus.DB_COMMITTED;

@Service
public class GraduationStatusService {

    private static Logger logger = LoggerFactory.getLogger(GraduationStatusService.class);

    @Autowired
    WebClient webClient;

    @Autowired
    private GraduationStudentRecordRepository graduationStatusRepository;

    @Autowired
    private GradStatusEventRepository gradStatusEventRepository;

    @Autowired
    private GraduationStatusTransformer graduationStatusTransformer;

    @Autowired
    private StudentOptionalProgramRepository gradStudentSpecialProgramRepository;

    @Autowired
    private GradStudentSpecialProgramTransformer gradStudentSpecialProgramTransformer;
    
    @Autowired
    private CommonService commonService;

    @Autowired
    private GradStudentService gradStudentService;
    
    @Autowired
    private HistoryService historyService;

    @Autowired
    GradValidation validation;

    @Autowired
    private EducGradStudentApiConstants constants;

    private static final String CREATE_USER = "createUser";
    private static final String CREATE_DATE = "createDate";


    public GraduationStudentRecord getGraduationStatusForAlgorithm(UUID studentID) {
        logger.info("getGraduationStatus");
        Optional<GraduationStudentRecordEntity> responseOptional = graduationStatusRepository.findById(studentID);
        if (responseOptional.isPresent()) {
            return graduationStatusTransformer.transformToDTO(responseOptional.get());
        } else {
            return null;
        }

    }

    public GraduationStudentRecord getGraduationStatus(UUID studentID, String accessToken) {
        logger.info("getGraduationStatus");
        Optional<GraduationStudentRecordEntity> responseOptional = graduationStatusRepository.findById(studentID);
        if (responseOptional.isPresent()) {
            GraduationStudentRecord gradStatus = graduationStatusTransformer.transformToDTO(responseOptional.get());
            if (gradStatus.getProgram() != null) {
                gradStatus.setProgramName(getProgramName(gradStatus.getProgram(), accessToken));
            }
            if (gradStatus.getSchoolOfRecord() != null)
                gradStatus.setSchoolName(getSchoolName(gradStatus.getSchoolOfRecord(), accessToken));

            if (gradStatus.getStudentStatus() != null) {
                StudentStatus statusObj = commonService.getSpecificStudentStatusCode(gradStatus.getStudentStatus());
                if (statusObj != null)
                    gradStatus.setStudentStatusName(statusObj.getDescription());
            }

            if (gradStatus.getSchoolAtGrad() != null)
                gradStatus.setSchoolAtGradName(getSchoolName(gradStatus.getSchoolAtGrad(), accessToken));

            return gradStatus;
        } else {
            return null;
        }

    }

    @Transactional
    public Pair<GraduationStudentRecord, GradStatusEvent> saveGraduationStatus(UUID studentID, GraduationStudentRecord graduationStatus, String accessToken) throws JsonProcessingException {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        GraduationStudentRecordEntity sourceObject = graduationStatusTransformer.transformToEntity(graduationStatus);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            BeanUtils.copyProperties(sourceObject, gradEntity, CREATE_USER, CREATE_DATE);
            gradEntity.setRecalculateGradStatus(null);
            gradEntity.setProgramCompletionDate(sourceObject.getProgramCompletionDate());
            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            historyService.createStudentHistory(gradEntity, "GRADALG");
            final GraduationStudentRecord savedGraduationStatus = graduationStatusTransformer.transformToDTO(gradEntity);
            final GradStatusEvent gradStatusEvent = createGradStatusEvent(gradEntity.getCreateUser(), gradEntity.getUpdateUser(),
                    savedGraduationStatus, EventType.UPDATE_GRAD_STATUS, EventOutcome.GRAD_STATUS_UPDATED, accessToken);
            gradStatusEventRepository.save(gradStatusEvent);
            return Pair.of(savedGraduationStatus, gradStatusEvent);
        } else {
            sourceObject = graduationStatusRepository.saveAndFlush(sourceObject);
            final GraduationStudentRecord savedGraduationStatus = graduationStatusTransformer.transformToDTO(sourceObject);
            final GradStatusEvent gradStatusEvent = createGradStatusEvent(sourceObject.getCreateUser(), sourceObject.getUpdateUser(),
                    savedGraduationStatus, EventType.CREATE_GRAD_STATUS, EventOutcome.GRAD_STATUS_CREATED, accessToken);
            gradStatusEventRepository.save(gradStatusEvent);
            return Pair.of(savedGraduationStatus, gradStatusEvent);
        }
    }

    @Transactional
    public Pair<GraduationStudentRecord, GradStatusEvent> updateGraduationStatus(UUID studentID, GraduationStudentRecord graduationStatus, String accessToken) throws JsonProcessingException {
        Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        GraduationStudentRecordEntity sourceObject = graduationStatusTransformer.transformToEntity(graduationStatus);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
            boolean hasDataChanged = validateData(sourceObject, gradEntity, accessToken);
            if (validation.hasErrors()) {
                validation.stopOnErrors();
                return Pair.of(new GraduationStudentRecord(), null);
            }
            if(hasDataChanged && !sourceObject.getProgram().equalsIgnoreCase(gradEntity.getProgram())) {
                deleteStudentOptionalPrograms(sourceObject.getStudentID());
            }
            if (hasDataChanged) {
                gradEntity.setRecalculateGradStatus("Y");
            } else {
                gradEntity.setRecalculateGradStatus(null);
            }
            BeanUtils.copyProperties(sourceObject, gradEntity, CREATE_USER, CREATE_DATE, "studentGradData", "recalculateGradStatus");
            gradEntity.setProgramCompletionDate(sourceObject.getProgramCompletionDate());
            gradEntity = graduationStatusRepository.saveAndFlush(gradEntity);
            historyService.createStudentHistory(gradEntity, "USEREDIT");
            final GraduationStudentRecord updatedGraduationStatus = graduationStatusTransformer.transformToDTO(gradEntity);
            final GradStatusEvent gradStatusEvent = createGradStatusEvent(gradEntity.getCreateUser(), gradEntity.getUpdateUser(),
                    updatedGraduationStatus, EventType.UPDATE_GRAD_STATUS, EventOutcome.GRAD_STATUS_UPDATED, accessToken);
            gradStatusEventRepository.save(gradStatusEvent);
            return Pair.of(updatedGraduationStatus, gradStatusEvent);
        } else {
            validation.addErrorAndStop(String.format("Student ID [%s] does not exists", studentID));
            return Pair.of(graduationStatus, null);
        }
    }

    private String getSchoolName(String minCode, String accessToken) {
        School schObj = webClient.get()
                .uri(String.format(constants.getSchoolByMincodeUrl(), minCode))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(School.class)
                .block();
        if (schObj != null)
            return schObj.getSchoolName();
        else
            return null;
    }

    private String getProgramName(String programCode, String accessToken) {
        GradProgram gradProgram = webClient.get()
                .uri(String.format(constants.getGradProgramNameUrl(), programCode))
                .headers(h -> h.setBearerAuth(accessToken))
                .retrieve()
                .bodyToMono(GradProgram.class)
                .block();
        if (gradProgram != null)
            return gradProgram.getProgramName();
        return null;
    }

    private void validateStudentStatus(String studentStatus) {
        if (studentStatus.equalsIgnoreCase("M")) {
            validation.addErrorAndStop("Student GRAD data cannot be updated for students with a status of 'M' merged");
        }
        if (studentStatus.equalsIgnoreCase("D")) {
            validation.addErrorAndStop("This student is showing as deceased.  Confirm the students' status before re-activating by setting their status to 'A' if they are currently attending school");
        }
    }

    private void validateProgram(GraduationStudentRecordEntity sourceEntity, String accessToken) {
        GradProgram gradProgram = webClient.get()
				.uri(String.format(constants.getGradProgramNameUrl(), sourceEntity.getProgram()))
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve()
				.bodyToMono(GradProgram.class)
				.block();
        if (gradProgram == null) {
            validation.addError(String.format("Program [%s] is invalid", sourceEntity.getProgram()));
        } else {
            if (sourceEntity.getProgram().contains("1950")) {
                if (!sourceEntity.getStudentGrade().equalsIgnoreCase("AD")
						&& !sourceEntity.getStudentGrade().equalsIgnoreCase("AN")) {
                    validation.addError(
                    		String.format("Student grade should be one of AD or AN if the student program is [%s]",
									sourceEntity.getProgram()));
                }
            } else {
                if (sourceEntity.getStudentGrade().equalsIgnoreCase("AD")
						|| sourceEntity.getStudentGrade().equalsIgnoreCase("AN")) {
                    validation.addError(
                    		String.format("Student grade should not be AD or AN for this program [%s]",
									sourceEntity.getProgram()));
                }
            }
        }
    }

    private void validateSchool(String minCode, String accessToken) {
        School schObj = webClient.get()
				.uri(String.format(constants.getSchoolByMincodeUrl(), minCode))
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve()
				.bodyToMono(School.class)
				.block();
        if (schObj == null) {
            validation.addError(
            		String.format("Invalid School entered, School [%s] does not exist on the School table", minCode));
        } else {
            if (schObj.getOpenFlag().equalsIgnoreCase("N")) {
                validation.addWarning(String.format("This School [%s] is Closed", minCode));
            }
        }
    }

    private void validateStudentGrade(GraduationStudentRecordEntity sourceEntity, String accessToken) {
        Student studentObj = webClient.get()
				.uri(String.format(constants.getPenStudentApiByStudentIdUrl(), sourceEntity.getStudentID()))
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve()
				.bodyToMono(Student.class)
				.block();
        if(sourceEntity.getStudentStatus() != null) {
	        if (sourceEntity.getStudentStatus().equalsIgnoreCase("D")
					|| sourceEntity.getStudentStatus().equalsIgnoreCase("M")) {
	            if (!sourceEntity.getStudentStatus().equalsIgnoreCase(studentObj.getStatusCode())) {
	                validation.addError("Status code selected does not match with the PEN data for this student");
	            }
	        } else {
	            if (!"A".equalsIgnoreCase(studentObj.getStatusCode())) {
	                validation.addError("Status code selected does not match with the PEN data for this student");
	            }
	        }
        }
        if (sourceEntity.getStudentGrade() != null && (sourceEntity.getStudentGrade().equalsIgnoreCase("AN")
				|| sourceEntity.getStudentGrade().equalsIgnoreCase("AD"))
				&& calculateAge(studentObj.getDob()) < 18) {
            validation.addError("Adult student should be at least 18 years old");
        }
        
    }

    private boolean validateData(GraduationStudentRecordEntity sourceEntity, GraduationStudentRecordEntity existingEntity, String accessToken) {
        boolean hasDataChangd = false;
        validateStudentStatus(existingEntity.getStudentStatus());
        if (!sourceEntity.getProgram().equalsIgnoreCase(existingEntity.getProgram())) {
            hasDataChangd = true;
            validateProgram(sourceEntity, accessToken);
        }
        
        if (sourceEntity.getSchoolOfRecord() != null && !sourceEntity.getSchoolOfRecord().equalsIgnoreCase(existingEntity.getSchoolOfRecord())) {
            hasDataChangd = true;
            validateSchool(sourceEntity.getSchoolOfRecord(), accessToken);
        }        
        
        if (sourceEntity.getSchoolAtGrad() != null && !sourceEntity.getSchoolAtGrad().equalsIgnoreCase(existingEntity.getSchoolAtGrad())) {
            hasDataChangd = true;
            validateSchool(sourceEntity.getSchoolAtGrad(), accessToken);
        }
        
        if ((sourceEntity.getStudentGrade() != null && !sourceEntity.getStudentGrade().equalsIgnoreCase(existingEntity.getStudentGrade()))
				|| (sourceEntity.getStudentStatus() != null && !sourceEntity.getStudentStatus().equalsIgnoreCase(existingEntity.getStudentStatus()))) {
            hasDataChangd = true;
            validateStudentGrade(sourceEntity, accessToken);
        }
        if (sourceEntity.getGpa() != null && !sourceEntity.getGpa().equalsIgnoreCase(existingEntity.getGpa())) {
            hasDataChangd = true;
            sourceEntity.setHonoursStanding(getHonoursFlag(sourceEntity.getGpa()));
        }
        return hasDataChangd;
    }

    private void deleteStudentOptionalPrograms(UUID studentID) {
        List<StudentOptionalProgramEntity> studOpList = gradStudentSpecialProgramRepository.findByStudentID(studentID);
        if(!studOpList.isEmpty()) {
            for (StudentOptionalProgramEntity studentOptionalProgramEntity : studOpList) {
                historyService.createStudentOptionalProgramHistory(studentOptionalProgramEntity,"USERDELETE");
                gradStudentSpecialProgramRepository.deleteById(studentOptionalProgramEntity.getId());
            }
        }
    }

    private String getHonoursFlag(String gPA) {
        if (Float.parseFloat(gPA) > 3)
            return "Y";
        else
            return "N";
    }

    public List<StudentOptionalProgram> getStudentGradSpecialProgram(UUID studentID, String accessToken) {
        List<StudentOptionalProgram> specialProgramList =
				gradStudentSpecialProgramTransformer.transformToDTO(gradStudentSpecialProgramRepository.findByStudentID(studentID));
        specialProgramList.forEach(sP -> {
            OptionalProgram gradSpecialProgram = webClient.get()
					.uri(String.format(constants.getGradSpecialProgramNameUrl(), sP.getOptionalProgramID()))
					.headers(h -> h.setBearerAuth(accessToken))
					.retrieve()
					.bodyToMono(OptionalProgram.class)
					.block();
            sP.setSpecialProgramName(gradSpecialProgram.getOptionalProgramName());
            sP.setSpecialProgramCode(gradSpecialProgram.getOptProgramCode());
            sP.setProgramCode(gradSpecialProgram.getGraduationProgramCode());
        });
        return specialProgramList;
    }

    public int calculateAge(String dob) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDate = LocalDate.parse(dob, dateFormatter);
        LocalDate currentDate = LocalDate.now();
        return Period.between(birthDate, currentDate).getYears();
    }

    public StudentOptionalProgram saveStudentGradSpecialProgram(StudentOptionalProgram gradStudentSpecialProgram) {
        Optional<StudentOptionalProgramEntity> gradStudentSpecialOptional =
				gradStudentSpecialProgramRepository.findById(gradStudentSpecialProgram.getId());
        StudentOptionalProgramEntity sourceObject = gradStudentSpecialProgramTransformer.transformToEntity(gradStudentSpecialProgram);
        if (gradStudentSpecialOptional.isPresent()) {
            StudentOptionalProgramEntity gradEnity = gradStudentSpecialOptional.get();
            BeanUtils.copyProperties(sourceObject, gradEnity, CREATE_USER, CREATE_DATE);
            gradEnity.setSpecialProgramCompletionDate(sourceObject.getSpecialProgramCompletionDate());
            gradEnity = gradStudentSpecialProgramRepository.save(gradEnity);
            historyService.createStudentOptionalProgramHistory(gradEnity,"GRADGALG");
            return gradStudentSpecialProgramTransformer.transformToDTO(gradEnity);
        } else {
            return gradStudentSpecialProgramTransformer.transformToDTO(gradStudentSpecialProgramRepository.save(sourceObject));
        }
    }
    
    public StudentOptionalProgram updateStudentGradSpecialProgram(StudentOptionalProgramReq gradStudentSpecialProgramReq,String accessToken) {
        Optional<StudentOptionalProgramEntity> gradStudentSpecialOptional = Optional.empty();
        if(gradStudentSpecialProgramReq.getId() != null)
            gradStudentSpecialOptional = gradStudentSpecialProgramRepository.findById(gradStudentSpecialProgramReq.getId());

        StudentOptionalProgramEntity sourceObject = new StudentOptionalProgramEntity();
        OptionalProgram gradSpecialProgram = webClient.get()
				.uri(String.format(constants.getGradSpecialProgramDetailsUrl(), gradStudentSpecialProgramReq.getMainProgramCode(),gradStudentSpecialProgramReq.getSpecialProgramCode()))
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve()
				.bodyToMono(OptionalProgram.class)
				.block();
        sourceObject.setId(gradStudentSpecialProgramReq.getId());
        sourceObject.setPen(gradStudentSpecialProgramReq.getPen());
        sourceObject.setStudentID(gradStudentSpecialProgramReq.getStudentID());
        sourceObject.setSpecialProgramCompletionDate(gradStudentSpecialProgramReq.getSpecialProgramCompletionDate() != null ?Date.valueOf(gradStudentSpecialProgramReq.getSpecialProgramCompletionDate()) : null);
        sourceObject.setOptionalProgramID(gradSpecialProgram.getOptionalProgramID());
        if (gradStudentSpecialOptional.isPresent()) {
            StudentOptionalProgramEntity gradEntity = gradStudentSpecialOptional.get();
            if(gradEntity.getOptionalProgramID().equals(sourceObject.getOptionalProgramID())) {
                sourceObject.setStudentSpecialProgramData(gradEntity.getStudentSpecialProgramData());
            }else {
                sourceObject.setStudentSpecialProgramData(null);
            }
            BeanUtils.copyProperties(sourceObject, gradEntity, CREATE_USER, CREATE_DATE,"id");
            gradEntity.setSpecialProgramCompletionDate(sourceObject.getSpecialProgramCompletionDate());
            gradEntity = gradStudentSpecialProgramRepository.save(gradEntity);
            historyService.createStudentOptionalProgramHistory(gradEntity, "USEREDIT");
            return gradStudentSpecialProgramTransformer.transformToDTO(gradEntity);
        } else {
            sourceObject = gradStudentSpecialProgramRepository.save(sourceObject);
            historyService.createStudentOptionalProgramHistory(sourceObject, "USEREDIT");
            return gradStudentSpecialProgramTransformer.transformToDTO(sourceObject);
        }
    }

    public List<GraduationStudentRecord> getStudentsForGraduation() {
        return graduationStatusTransformer.transformToDTO(graduationStatusRepository.findByRecalculateGradStatus("Y"));
    }

    public StudentOptionalProgram getStudentGradSpecialProgramByProgramCodeAndSpecialProgramCode(
    		UUID studentID, String specialProgramID, String accessToken) {
        UUID specialProgramIDUUID = UUID.fromString(specialProgramID);
        Optional<StudentOptionalProgramEntity> gradStudentSpecialOptional =
				gradStudentSpecialProgramRepository.findByStudentIDAndOptionalProgramID(studentID, specialProgramIDUUID);
        if (gradStudentSpecialOptional.isPresent()) {
            StudentOptionalProgram responseObj = gradStudentSpecialProgramTransformer.transformToDTO(gradStudentSpecialOptional);
            OptionalProgram gradSpecialProgram = webClient.get()
					.uri(String.format(constants.getGradSpecialProgramNameUrl(), responseObj.getOptionalProgramID()))
					.headers(h -> h.setBearerAuth(accessToken))
					.retrieve()
					.bodyToMono(OptionalProgram.class)
					.block();
            responseObj.setSpecialProgramName(gradSpecialProgram.getOptionalProgramName());
            responseObj.setSpecialProgramCode(gradSpecialProgram.getOptProgramCode());
            responseObj.setProgramCode(gradSpecialProgram.getGraduationProgramCode());
            return responseObj;
        }
        return null;
    }

    public boolean getStudentStatus(String statusCode) {
        List<GraduationStudentRecordEntity> gradList = graduationStatusRepository.existsByStatusCode(statusCode);
        return !gradList.isEmpty();
    }

    @Transactional
    public Pair<GraduationStudentRecord, GradStatusEvent> ungradStudent(UUID studentID, String ungradReasonCode, String ungradDesc, String accessToken) throws JsonProcessingException {
        if(StringUtils.isNotBlank(ungradReasonCode)) {
        	UngradReason ungradReasonObj = webClient.get().uri(String.format(constants.getUngradReasonDetailsUrl(),ungradReasonCode)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(UngradReason.class).block();
    		if(ungradReasonObj != null) {
		    	Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
		        if (gradStatusOptional.isPresent()) {
		            GraduationStudentRecordEntity gradEntity = gradStatusOptional.get();
		            saveUngradReason(studentID,ungradReasonCode,ungradDesc,accessToken);
		            deleteStudentAchievements(studentID,accessToken);
                    gradEntity.setRecalculateGradStatus("Y");
                    gradEntity.setProgramCompletionDate(null);
                    gradEntity.setHonoursStanding(null);
                    gradEntity.setGpa(null);
                    gradEntity.setSchoolAtGrad(null);
                    gradEntity = graduationStatusRepository.save(gradEntity);
                    historyService.createStudentHistory(gradEntity, "USERUNGRAD");
                    final GraduationStudentRecord graduationStatus = graduationStatusTransformer.transformToDTO(gradEntity);
                    final GradStatusEvent gradStatusEvent = createGradStatusEvent(gradEntity.getCreateUser(), gradEntity.getUpdateUser(),
                            graduationStatus, EventType.UPDATE_GRAD_STATUS, EventOutcome.GRAD_STATUS_UPDATED, accessToken);
                    gradStatusEventRepository.save(gradStatusEvent);
                    return Pair.of(graduationStatus, gradStatusEvent);
		        } else {
		            validation.addErrorAndStop(String.format("Student ID [%s] does not exists", studentID));
		            return Pair.of(null, null);
		        }
    		}else {
    			validation.addErrorAndStop(String.format("Invalid Ungrad Reason Code [%s]",ungradReasonCode));
                return Pair.of(null, null);
    		}
        }else {
        	validation.addErrorAndStop("Ungrad Reason Code is required");
            return Pair.of(null, null);
        }
    }
    
    private void deleteStudentAchievements(UUID studentID,String accessToken) {
    	webClient.delete().uri(String.format(constants.getDeleteStudentAchievements(), studentID)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(Integer.class).block();
	}

	public void saveUngradReason(UUID studentID, String ungradReasonCode, String unGradDesc,String accessToken) {
        StudentUngradReason toBeSaved = new StudentUngradReason();
        toBeSaved.setGraduationStudentRecordID(studentID);
        toBeSaved.setUngradReasonCode(ungradReasonCode);
        toBeSaved.setUngradReasonDescription(unGradDesc);
        webClient.post().uri(String.format(constants.getSaveStudentUngradReasonByStudentIdUrl(),studentID)).headers(h -> h.setBearerAuth(accessToken)).body(BodyInserters.fromValue(toBeSaved)).retrieve().bodyToMono(GradStudentUngradReasons.class).block();
    }

    private GradStatusEvent createGradStatusEvent(String createUser, String updateUser,
                                                  GraduationStudentRecord graduationStatus,
                                                  EventType eventType, EventOutcome eventOutcome,
                                                  String accessToken) throws JsonProcessingException {
        if (StringUtils.isBlank(graduationStatus.getPen())) {
            GradSearchStudent gradSearchStudent = gradStudentService.getStudentByStudentIDFromStudentAPI(graduationStatus.getStudentID().toString(), accessToken);
            if (gradSearchStudent != null) {
                graduationStatus.setPen(gradSearchStudent.getPen());
            }
        }
        String jsonString = JsonUtil.getJsonStringFromObject(graduationStatus);
        return GradStatusEvent.builder()
                .createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now())
                .createUser(StringUtils.isBlank(createUser)? EducGradStudentApiConstants.DEFAULT_CREATED_BY : createUser)
                .updateUser(StringUtils.isBlank(updateUser)? EducGradStudentApiConstants.DEFAULT_UPDATED_BY : updateUser)
                .eventPayload(jsonString)
                .eventType(eventType.toString())
                .eventStatus(DB_COMMITTED.toString())
                .eventOutcome(eventOutcome.toString())
                .build();
    }

	public boolean restoreGradStudentRecord(UUID studentID,boolean isGraduated) {
		Optional<GraduationStudentRecordEntity> gradStatusOptional = graduationStatusRepository.findById(studentID);
        if (gradStatusOptional.isPresent()) {
            GraduationStudentRecordEntity gradEnity = gradStatusOptional.get();
            gradEnity.setRecalculateGradStatus("Y");
            if(!isGraduated) {
	            gradEnity.setProgramCompletionDate(null);
	            gradEnity.setHonoursStanding(null);
	            gradEnity.setGpa(null);
	            gradEnity.setSchoolAtGrad(null);
            }
            graduationStatusRepository.save(gradEnity);
            return true;
        }
        return false;
	}
  
}
