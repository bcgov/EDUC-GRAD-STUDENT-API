package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.*;
import ca.bc.gov.educ.api.gradstudent.model.transformer.*;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CommonService {
	private static final Logger logger = LoggerFactory.getLogger(CommonService.class);
    
    private static final String CREATE_USER="createUser";
	private static final String CREATE_DATE="createDate";

	final StudentCareerProgramRepository gradStudentCareerProgramRepository;
	final GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer;
	final StudentNoteTransformer  studentNoteTransformer;
	final StudentNoteRepository studentNoteRepository;
	final EducGradStudentApiConstants constants;
	final StudentStatusRepository studentStatusRepository;
	final StudentStatusTransformer studentStatusTransformer;
	final GraduationStatusService graduationStatusService;
	final GradStudentService gradStudentService;
	final WebClient studentApiClient;
	final GradValidation validation;
	final HistoryActivityRepository historyActivityRepository;
	final HistoryActivityTransformer historyActivityTransformer;
	final StudentGradeCodeRepository studentGradeCodeRepository;
	final StudentGradeCodeTransformer studentGradeCodeTransformer;

	@Autowired
	public CommonService(EducGradStudentApiConstants constants,
						 StudentCareerProgramRepository gradStudentCareerProgramRepository,
						 GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer,
						 StudentNoteTransformer studentNoteTransformer,
						 StudentNoteRepository studentNoteRepository,
						 StudentStatusRepository studentStatusRepository,
						 StudentStatusTransformer studentStatusTransformer,
						 GraduationStatusService graduationStatusService,
						 GradStudentService gradStudentService,
						 HistoryActivityRepository historyActivityRepository,
						 HistoryActivityTransformer historyActivityTransformer,
						 @Qualifier("studentApiClient") WebClient studentApiClient,
						 GradValidation validation,
						 StudentGradeCodeRepository studentGradeCodeRepository,
						 StudentGradeCodeTransformer studentGradeCodeTransformer) {
		this.constants = constants;
		this.gradStudentCareerProgramRepository = gradStudentCareerProgramRepository;
		this.gradStudentCareerProgramTransformer = gradStudentCareerProgramTransformer;
		this.studentNoteTransformer = studentNoteTransformer;
		this.studentNoteRepository = studentNoteRepository;
		this.studentStatusRepository = studentStatusRepository;
		this.studentStatusTransformer = studentStatusTransformer;
		this.graduationStatusService = graduationStatusService;
		this.gradStudentService = gradStudentService;
		this.historyActivityRepository = historyActivityRepository;
		this.historyActivityTransformer = historyActivityTransformer;
		this.studentApiClient = studentApiClient;
		this.validation = validation;
		this.studentGradeCodeRepository = studentGradeCodeRepository;
		this.studentGradeCodeTransformer = studentGradeCodeTransformer;
	}

	@Transactional
  	public List<StudentCareerProgram> getAllGradStudentCareerProgramList(String studentId, String accessToken) {
		logger.debug("getAllGradStudentCareerProgramList");
		List<StudentCareerProgram> gradStudentCareerProgramList  = gradStudentCareerProgramTransformer.transformToDTO(gradStudentCareerProgramRepository.findByStudentID(UUID.fromString(studentId)));
      	gradStudentCareerProgramList.forEach(sC -> {
      		CareerProgram gradCareerProgram= studentApiClient.get().uri(String.format(constants.getCareerProgramByCodeUrl(),sC.getCareerProgramCode()))
									.headers(h -> h.setBearerAuth(accessToken))
					.retrieve().bodyToMono(CareerProgram.class).block();
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
		String userName = ThreadLocalStateUtil.getCurrentUser();
		toBeSaved.setCreateDate(LocalDateTime.now());
		toBeSaved.setCreateUser(userName);
		if(studentNote.getId() != null) {
			Optional<StudentRecordNoteEntity> existingEnity = studentNoteRepository.findById(studentNote.getId());
			if(existingEnity.isPresent()) {
				StudentRecordNoteEntity gradEntity = existingEnity.get();
				if(studentNote.getNote() != null) {
					gradEntity.setUpdateUser(userName);
					gradEntity.setUpdateDate(LocalDateTime.now());
					gradEntity.setNote(studentNote.getNote());
				}
				if(studentNote.getStudentID() != null) {
					gradEntity.setStudentID(UUID.fromString(studentNote.getStudentID()));
				}
				return studentNoteTransformer.transformToDTO(studentNoteRepository.save(gradEntity));
			}
		}

		if(studentNote.getStudentID() != null) {
			toBeSaved.setStudentID(UUID.fromString(studentNote.getStudentID()));
		}
		return studentNoteTransformer.transformToDTO(studentNoteRepository.save(toBeSaved));
	}

	public List<StudentNote> saveStudentNotes(List<StudentNote> studentNotes) {
		List<StudentRecordNoteEntity> recordNoteEntities = studentNotes.stream()
				.map(studentNote -> {
					StudentRecordNoteEntity entity = studentNoteTransformer.transformToEntity(studentNote);
					entity.setUpdateUser(ThreadLocalStateUtil.getCurrentUser());
					return entity;
				})
				.toList();
		List<StudentNote> responseList = studentNoteTransformer.transformToDTO(studentNoteRepository.saveAll(recordNoteEntities));

		responseList.sort(Comparator.comparing(StudentNote::getUpdateDate).reversed());
		return responseList;
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

	@Transactional
	public GradStudentAlgorithmData getGradStudentAlgorithmData(String studentID,String accessToken) {
		GradStudentAlgorithmData data = new GradStudentAlgorithmData();
		GradSearchStudent gradStudent = gradStudentService.getStudentByStudentIDFromStudentAPI(studentID);
		GraduationStudentRecord gradStudentRecord = graduationStatusService.getGraduationStatusForAlgorithm(UUID.fromString(studentID));
		List<StudentCareerProgram> cpList = getAllGradStudentCareerProgramList(studentID, accessToken);
		if(gradStudentRecord != null && StringUtils.isNotBlank(gradStudentRecord.getStudentCitizenship())) {
			gradStudent.setStudentCitizenship(gradStudentRecord.getStudentCitizenship());
		}
		data.setGradStudent(gradStudent);
		data.setGraduationStudentRecord(gradStudentRecord);
		data.setStudentCareerProgramList(cpList);
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

	@Transactional
	public List<UUID> getDeceasedStudentIDs(List<UUID> studentIDs) {
		return gradStudentService.getStudentIDsByStatusCode(studentIDs, "DEC");
	}

	public List<StudentGradeCode> getAllStudentGradeCodes() {
		return studentGradeCodeRepository.findAll().stream().map(studentGradeCodeTransformer::transformToDTO).toList();
	}
}
