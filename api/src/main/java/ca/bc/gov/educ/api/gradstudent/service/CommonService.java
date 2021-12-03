package ca.bc.gov.educ.api.gradstudent.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.transaction.Transactional;
import javax.validation.Valid;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.HistoryActivityCodeEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.HistoryActivityTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.HistoryActivityRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCareerProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentRecordNoteEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentStatusEntity;
import ca.bc.gov.educ.api.gradstudent.repository.StudentCareerProgramRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentNoteRepository;
import ca.bc.gov.educ.api.gradstudent.repository.StudentStatusRepository;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentCareerProgramTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.StudentNoteTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.StudentStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;


@Service
public class CommonService {
    
    @Autowired StudentCareerProgramRepository gradStudentCareerProgramRepository;
    @Autowired GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer;
    @Autowired StudentNoteTransformer  studentNoteTransformer;
    @Autowired StudentNoteRepository studentNoteRepository;
    @Autowired EducGradStudentApiConstants constants;
    @Autowired StudentStatusRepository studentStatusRepository;
	@Autowired StudentStatusTransformer studentStatusTransformer;
	@Autowired GraduationStatusService graduationStatusService;
	@Autowired GradStudentService gradStudentService;
    @Autowired WebClient webClient;
    @Autowired GradValidation validation;
	@Autowired HistoryActivityRepository historyActivityRepository;
	@Autowired HistoryActivityTransformer historyActivityTransformer;


	private static final Logger logger = LoggerFactory.getLogger(CommonService.class);
    
    private static final String CREATE_USER="createUser";
	private static final String CREATE_DATE="createDate";

    @Transactional
  	public List<StudentCareerProgram> getAllGradStudentCareerProgramList(String studentId, String accessToken) {
		logger.debug("getAllGradStudentCareerProgramList");
		List<StudentCareerProgram> gradStudentCareerProgramList  = gradStudentCareerProgramTransformer.transformToDTO(gradStudentCareerProgramRepository.findByStudentID(UUID.fromString(studentId)));
      	gradStudentCareerProgramList.forEach(sC -> {
      		CareerProgram gradCareerProgram= webClient.get().uri(String.format(constants.getCareerProgramByCodeUrl(),sC.getCareerProgramCode())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(CareerProgram.class).block();
    		if(gradCareerProgram != null) {
    			sC.setCareerProgramCode(gradCareerProgram.getCode());
    			sC.setCareerProgramName(gradCareerProgram.getDescription());
    		}
    	});
      	return gradStudentCareerProgramList;
  	}

	public boolean getStudentCareerProgram(String cpCode) {
		List<StudentCareerProgramEntity> gradList = gradStudentCareerProgramRepository.existsByCareerProgramCode(cpCode);
		return !gradList.isEmpty();
	}

	public List<StudentNote> getAllStudentNotes(UUID studentId) {
		List<StudentNote> responseList = studentNoteTransformer.transformToDTO(studentNoteRepository.findByStudentID(studentId));
		responseList.sort(Comparator.comparing(StudentNote::getUpdateDate).reversed());
		return responseList;
	}

	public StudentNote saveStudentNote(StudentNote studentNote) {
		logger.debug("saveStudentNote");
		StudentRecordNoteEntity toBeSaved = studentNoteTransformer.transformToEntity(studentNote);
		if(studentNote.getId() != null) {
			Optional<StudentRecordNoteEntity> existingEnity = studentNoteRepository.findById(studentNote.getId());
			if(existingEnity.isPresent()) {
				StudentRecordNoteEntity gradEntity = existingEnity.get();
				if(studentNote.getNote() != null) {
					gradEntity.setNote(studentNote.getNote());
				}
				if(studentNote.getStudentID() != null) {
					gradEntity.setStudentID(UUID.fromString(studentNote.getStudentID()));
				}
				return studentNoteTransformer.transformToDTO(studentNoteRepository.save(gradEntity));
			}else {
				if(studentNote.getStudentID() != null) {
					toBeSaved.setStudentID(UUID.fromString(studentNote.getStudentID()));
				}
			}
		}else {
			if(studentNote.getStudentID() != null) {
				toBeSaved.setStudentID(UUID.fromString(studentNote.getStudentID()));
			}
			
		}
		return studentNoteTransformer.transformToDTO(studentNoteRepository.save(toBeSaved));
	}

	public int deleteNote(UUID noteID) {
		Optional<StudentRecordNoteEntity> existingEnity = studentNoteRepository.findById(noteID);
		if(existingEnity.isPresent()) {
			studentNoteRepository.deleteById(noteID);
			return 1;
		}else {
			return 0;
		}
	}
	
	@Transactional
	public List<StudentStatus> getAllStudentStatusCodeList() {
		return studentStatusTransformer.transformToDTO(studentStatusRepository.findAll());
	}

	@Transactional
	public StudentStatus getSpecificStudentStatusCode(String statusCode) {
		logger.debug("getSpecificStudentStatusCode");
		Optional<StudentStatusEntity> entity = studentStatusRepository.findById(StringUtils.toRootUpperCase(statusCode));
		if (entity.isPresent()) {
			return studentStatusTransformer.transformToDTO(entity);
		} else {
			return null;
		}
	}

	public StudentStatus createStudentStatus(@Valid StudentStatus studentStatus) {
		StudentStatusEntity toBeSavedObject = studentStatusTransformer.transformToEntity(studentStatus);
		Optional<StudentStatusEntity> existingObjectCheck = studentStatusRepository.findById(studentStatus.getCode());
		if(existingObjectCheck.isPresent()) {
			validation.addErrorAndStop(String.format("Student Status Code [%s] already exists",studentStatus.getCode()));
			return studentStatus;			
		}else {
			return studentStatusTransformer.transformToDTO(studentStatusRepository.save(toBeSavedObject));
		}	
	}

	public StudentStatus updateStudentStatus(@Valid StudentStatus studentStatus) {
		Optional<StudentStatusEntity> studentStatusOptional = studentStatusRepository.findById(studentStatus.getCode());
		StudentStatusEntity sourceObject = studentStatusTransformer.transformToEntity(studentStatus);
		if(studentStatusOptional.isPresent()) {
			StudentStatusEntity gradEnity = studentStatusOptional.get();			
			BeanUtils.copyProperties(sourceObject,gradEnity,CREATE_USER,CREATE_DATE);
    		return studentStatusTransformer.transformToDTO(studentStatusRepository.save(gradEnity));
		}else {
			validation.addErrorAndStop(String.format("Student Status Code [%s] does not exists",studentStatus.getCode()));
			return studentStatus;
		}
	}

	public int deleteStudentStatus(@Valid String statusCode) {
		boolean isPresent = graduationStatusService.getStudentStatus(statusCode);
		if(isPresent) {
			validation.addErrorAndStop(
					String.format("This Student Status [%s] cannot be deleted as some students have this status associated with them.",statusCode));
			return 0;
		}else {
			studentStatusRepository.deleteById(statusCode);
			return 1;
		}
		
	}

	public GradStudentAlgorithmData getGradStudentAlgorithmData(String studentID,String accessToken) {
		GradStudentAlgorithmData data = new GradStudentAlgorithmData();
		GradSearchStudent gradStudent = gradStudentService.getStudentByStudentIDFromStudentAPI(studentID, accessToken);
		GraduationStudentRecord gradStudentRecord = graduationStatusService.getGraduationStatusForAlgorithm(UUID.fromString(studentID));
		data.setGradStudent(gradStudent);
		data.setGraduationStudentRecord(gradStudentRecord);		
		return data;
	}

	@Transactional
	public List<HistoryActivity> getAllHistoryActivityCodeList() {
		return historyActivityTransformer.transformToDTO(historyActivityRepository.findAll());
	}

	@Transactional
	public HistoryActivity getSpecificHistoryActivityCode(String activityCode) {
		logger.debug("getSpecificStudentStatusCode");
		Optional<HistoryActivityCodeEntity> entity = historyActivityRepository.findById(StringUtils.toRootUpperCase(activityCode));
		if (entity.isPresent()) {
			return historyActivityTransformer.transformToDTO(entity);
		} else {
			return null;
		}
	}
}
