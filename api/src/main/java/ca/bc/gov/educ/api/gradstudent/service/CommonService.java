package ca.bc.gov.educ.api.gradstudent.service;


import ca.bc.gov.educ.api.gradstudent.dto.*;
import ca.bc.gov.educ.api.gradstudent.entity.*;
import ca.bc.gov.educ.api.gradstudent.repository.*;
import ca.bc.gov.educ.api.gradstudent.transformer.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradCommonApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.util.*;


@Service
public class CommonService {
    
    @Autowired
    private GradStudentCareerProgramRepository gradStudentCareerProgramRepository;

	@Autowired
	private GradStudentSpecialProgramRepository gradStudentSpecialProgramRepository;

    @Autowired
    private GradStudentCareerProgramTransformer gradStudentCareerProgramTransformer;

	@Autowired
	private GradStudentSpecialProgramTransformer gradStudentSpecialProgramTransformer;
    
    @Autowired
    private StudentNoteTransformer  studentNoteTransformer;

	@Autowired
	private GradCareerProgramTransformer gradCareerProgramTransformer;

	@Autowired
	private GradSpecialProgramTransformer gradSpecialProgramTransformer;
    
    @Autowired
    private StudentNoteRepository studentNoteRepository;

	@Autowired
	private GradCareerProgramRepository gradCareerProgramRepository;

	@Autowired
	private GradSpecialProgramRepository gradSpecialProgramRepository;

    @Autowired
	private EducGradCommonApiConstants constants;
    
    @Autowired
    WebClient webClient;
    
    @Autowired
    RestTemplate restTemplate;
    
    @Autowired
	GradValidation validation;

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.getLogger(CommonService.class);

    @Transactional
  	public List<GradStudentCareerProgram> getAllGradStudentCareerProgramList(String studentId, String accessToken) {
		List<GradStudentCareerProgram> gradStudentCareerProgramList  = gradStudentCareerProgramTransformer.transformToDTO(gradStudentCareerProgramRepository.findByStudentID(UUID.fromString(studentId)));
      	gradStudentCareerProgramList.forEach(sC -> {
			GradCareerProgramEntity gradCareerProgramEntity = gradCareerProgramRepository.existsByCareerProgramCode(sC.getCareerProgramCode());
      		GradCareerProgram gradCareerProgram = gradCareerProgramTransformer.transformToDTO(gradCareerProgramEntity);
    		if(gradCareerProgram != null) {
    			sC.setCareerProgramCode(gradCareerProgram.getCode());
    			sC.setCareerProgramName(gradCareerProgram.getDescription());
    		}
    	});
      	return gradStudentCareerProgramList;
  	}

	@Transactional
	public List<GradStudentSpecialProgram> getAllGradStudentSpecialProgramList(String studentId, String accessToken) {
		List<GradStudentSpecialProgram> gradStudentSpecialProgramList  = gradStudentSpecialProgramTransformer.transformToDTO(gradStudentSpecialProgramRepository.findByStudentID(UUID.fromString(studentId)));
		gradStudentSpecialProgramList.forEach(sC -> {
			GradSpecialProgramEntity gradSpecialProgramEntity = gradSpecialProgramRepository.existsBySpecialProgramCode(sC.getSpecialProgramCode());
			GradSpecialProgram gradSpecialProgram = gradSpecialProgramTransformer.transformToDTO(gradSpecialProgramEntity);
			if(gradSpecialProgram != null) {
				sC.setSpecialProgramCode(gradSpecialProgram.getProgramCode());
				sC.setSpecialProgramName(gradSpecialProgram.getSpecialProgramName());
			}
		});
		return gradStudentSpecialProgramList;
	}

	public boolean getStudentCareerProgram(String cpCode) {
		List<GradStudentCareerProgramEntity> gradList = gradStudentCareerProgramRepository.existsByCareerProgramCode(cpCode);
		return !gradList.isEmpty();
	}

	public boolean getStudentSpecialProgram(String cpCode) {
		List<GradStudentSpecialProgramEntity> gradList = gradStudentSpecialProgramRepository.existsBySpecialProgramCode(cpCode);
		return !gradList.isEmpty();
	}

	public List<StudentNote> getAllStudentNotes(UUID studentId) {
		List<StudentNote> responseList = studentNoteTransformer.transformToDTO(studentNoteRepository.findByStudentID(studentId));
		Collections.sort(responseList, Comparator.comparing(StudentNote::getUpdatedTimestamp).reversed());
		return responseList;
	}

	public StudentNote saveStudentNote(StudentNote studentNote) {
		StudentNoteEntity toBeSaved = studentNoteTransformer.transformToEntity(studentNote);
		if(studentNote.getId() != null) {
			Optional<StudentNoteEntity> existingEnity = studentNoteRepository.findById(studentNote.getId());
			if(existingEnity.isPresent()) {
				StudentNoteEntity gradEntity = existingEnity.get();
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
		Optional<StudentNoteEntity> existingEnity = studentNoteRepository.findById(noteID);
		if(existingEnity.isPresent()) {
			studentNoteRepository.deleteById(noteID);
			return 1;
		}else {
			return 0;
		}
	}
}
