package ca.bc.gov.educ.api.gradstudent.service;


import java.util.List;
import java.util.UUID;

import ca.bc.gov.educ.api.gradstudent.model.dto.OptionalProgram;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecordHistory;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordHistoryRepository;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStudentRecordHistoryTransformer;

@Service
public class HistoryService {

    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);

    final
    WebClient webClient;

    final GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository;
    final GraduationStudentRecordHistoryTransformer graduationStudentRecordHistoryTransformer;
    final StudentOptionalProgramHistoryRepository studentOptionalProgramHistoryRepository;
    final StudentOptionalProgramHistoryTransformer studentOptionalProgramHistoryTransformer;
    final EducGradStudentApiConstants constants;

    @Autowired
    public HistoryService(WebClient webClient, GraduationStudentRecordHistoryRepository graduationStudentRecordHistoryRepository, GraduationStudentRecordHistoryTransformer graduationStudentRecordHistoryTransformer, StudentOptionalProgramHistoryRepository studentOptionalProgramHistoryRepository, StudentOptionalProgramHistoryTransformer studentOptionalProgramHistoryTransformer, EducGradStudentApiConstants constants) {
        this.webClient = webClient;
        this.graduationStudentRecordHistoryRepository = graduationStudentRecordHistoryRepository;
        this.graduationStudentRecordHistoryTransformer = graduationStudentRecordHistoryTransformer;
        this.studentOptionalProgramHistoryRepository = studentOptionalProgramHistoryRepository;
        this.studentOptionalProgramHistoryTransformer = studentOptionalProgramHistoryTransformer;
        this.constants = constants;
    }

    public void createStudentHistory(GraduationStudentRecordEntity curStudentEntity, String historyActivityCode) {
    	logger.debug("Create Student History");
    	final GraduationStudentRecordHistoryEntity graduationStudentRecordHistoryEntity = new GraduationStudentRecordHistoryEntity();
        BeanUtils.copyProperties(curStudentEntity, graduationStudentRecordHistoryEntity);
        graduationStudentRecordHistoryEntity.setActivityCode(historyActivityCode);
        graduationStudentRecordHistoryRepository.save(graduationStudentRecordHistoryEntity);
    }

    public void createStudentOptionalProgramHistory(StudentOptionalProgramEntity curStudentOptionalProgramEntity, String historyActivityCode) {
        logger.debug("Create Student Optional History");
        final StudentOptionalProgramHistoryEntity studentOptionalProgramHistoryEntity = new StudentOptionalProgramHistoryEntity();
        BeanUtils.copyProperties(curStudentOptionalProgramEntity, studentOptionalProgramHistoryEntity);
        studentOptionalProgramHistoryEntity.setStudentOptionalProgramID(curStudentOptionalProgramEntity.getId());
        studentOptionalProgramHistoryEntity.setActivityCode(historyActivityCode);
        studentOptionalProgramHistoryRepository.save(studentOptionalProgramHistoryEntity);
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
            if(gradOptionalProgram != null) {
                sP.setOptionalProgramName(gradOptionalProgram.getOptionalProgramName());
                sP.setOptionalProgramCode(gradOptionalProgram.getOptProgramCode());
                sP.setProgramCode(gradOptionalProgram.getGraduationProgramCode());
            }
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

    public Page<GraduationStudentRecordHistoryEntity> getStudentHistoryByBatchID(Long batchId, Integer pageNumber, Integer pageSize) {
        Pageable paging = PageRequest.of(pageNumber, pageSize);
        Page<GraduationStudentRecordHistoryEntity> pagedResult = graduationStudentRecordHistoryRepository.findByBatchId(batchId,paging);
        return pagedResult;
    }
}
