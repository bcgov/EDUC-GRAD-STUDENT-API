package ca.bc.gov.educ.api.gradstudent.service;


import java.util.List;
import java.util.UUID;

import ca.bc.gov.educ.api.gradstudent.model.dto.OptionalProgram;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgramHistory;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.StudentOptionalProgramHistoryTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecordHistory;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStudentRecordHistoryTransformer;

@Service
public class HistoryService {

    private static Logger logger = LoggerFactory.getLogger(HistoryService.class);

    @Autowired
    WebClient webClient;

    @Autowired
    private GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository;  

    @Autowired
    private GraduationStudentRecordHistoryTransformer graduationStudentRecordHistoryTransformer;

    @Autowired
    private StudentOptionalProgramHistoryRepository studentOptionalProgramHistoryRepository;

    @Autowired
    private StudentOptionalProgramHistoryTransformer studentOptionalProgramHistoryTransformer;

    @Autowired
    private EducGradStudentApiConstants constants;

    public GraduationStudentRecordHistoryEntity createStudentHistory(GraduationStudentRecordEntity curStudentEntity, String historyActivityCode) {
    	logger.info("Create Student History");
    	final GraduationStudentRecordHistoryEntity graduationStudentRecordHistoryEntity = new GraduationStudentRecordHistoryEntity();
        BeanUtils.copyProperties(curStudentEntity, graduationStudentRecordHistoryEntity);
        graduationStudentRecordHistoryEntity.setActivityCode(historyActivityCode);
        return graduationStudentRecordHistoryRepository.save(graduationStudentRecordHistoryEntity);
    }

    public StudentOptionalProgramHistoryEntity createStudentOptionalProgramHistory(StudentOptionalProgramEntity curStudentOptionalProgramEntity, String historyActivityCode) {
        logger.info("Create Student History");
        final StudentOptionalProgramHistoryEntity studentOptionalProgramHistoryEntity = new StudentOptionalProgramHistoryEntity();
        BeanUtils.copyProperties(curStudentOptionalProgramEntity, studentOptionalProgramHistoryEntity);
        studentOptionalProgramHistoryEntity.setStudentOptionalProgramID(curStudentOptionalProgramEntity.getId());
        studentOptionalProgramHistoryEntity.setActivityCode(historyActivityCode);
        return studentOptionalProgramHistoryRepository.save(studentOptionalProgramHistoryEntity);
    }
    
    public List<GraduationStudentRecordHistory> getStudentEditHistory(UUID studentID) {
        return graduationStudentRecordHistoryTransformer.transformToDTO(graduationStudentRecordHistoryRepository.findByStudentID(studentID));
    }

    public List<StudentOptionalProgramHistory> getStudentOptionalProgramEditHistory(UUID studentID,String accessToken) {
        List<StudentOptionalProgramHistory> histList =   studentOptionalProgramHistoryTransformer.transformToDTO(studentOptionalProgramHistoryRepository.findByStudentID(studentID));
        histList.forEach(sP -> {
            OptionalProgram gradOptionalProgram = webClient.get()
                    .uri(String.format(constants.getGradOptionalProgramNameUrl(), sP.getOptionalProgramID()))
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(OptionalProgram.class)
                    .block();
            sP.setOptionalProgramName(gradOptionalProgram.getOptionalProgramName());
            sP.setOptionalProgramCode(gradOptionalProgram.getOptProgramCode());
            sP.setProgramCode(gradOptionalProgram.getGraduationProgramCode());
        });
        return histList;
    }

    public GraduationStudentRecordHistory getStudentHistoryByID(UUID historyID) {
        return graduationStudentRecordHistoryTransformer.transformToDTO(graduationStudentRecordHistoryRepository.findById(historyID));
    }

    public StudentOptionalProgramHistory getStudentOptionalProgramHistoryByID(UUID historyID,String accessToken) {
        StudentOptionalProgramHistory obj = studentOptionalProgramHistoryTransformer.transformToDTO(studentOptionalProgramHistoryRepository.findById(historyID));
        if(obj.getOptionalProgramID() != null) {
            OptionalProgram gradOptionalProgram = webClient.get()
                    .uri(String.format(constants.getGradOptionalProgramNameUrl(), obj.getOptionalProgramID()))
                    .headers(h -> h.setBearerAuth(accessToken))
                    .retrieve()
                    .bodyToMono(OptionalProgram.class)
                    .block();
            if(gradOptionalProgram != null) {
                obj.setOptionalProgramName(gradOptionalProgram.getOptionalProgramName());
                obj.setOptionalProgramCode(gradOptionalProgram.getOptProgramCode());
                obj.setProgramCode(gradOptionalProgram.getGraduationProgramCode());
            }
        }
        return obj;
    }
}
