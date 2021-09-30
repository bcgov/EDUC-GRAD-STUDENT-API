package ca.bc.gov.educ.api.gradstudent.service;


import java.util.List;
import java.util.UUID;

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


    public GraduationStudentRecordHistoryEntity createStudentHistory(GraduationStudentRecordEntity curStudentEntity, String historyActivityCode) {
    	logger.info("Create Student History");
    	final GraduationStudentRecordHistoryEntity graduationStudentRecordHistoryEntity = new GraduationStudentRecordHistoryEntity();
        BeanUtils.copyProperties(curStudentEntity, graduationStudentRecordHistoryEntity);
        graduationStudentRecordHistoryEntity.setActivityCode(historyActivityCode);
        return graduationStudentRecordHistoryRepository.save(graduationStudentRecordHistoryEntity);
    }
    
    public List<GraduationStudentRecordHistory> getStudentEditHistory(UUID studentID) {
        return graduationStudentRecordHistoryTransformer.transformToDTO(graduationStudentRecordHistoryRepository.findByStudentID(studentID));
    }
    
  
}
