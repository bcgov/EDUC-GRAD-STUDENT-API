package ca.bc.gov.educ.api.gradstudent.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradStudentRepository;
import ca.bc.gov.educ.api.gradstudent.repository.SchoolRepository;
import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;
import ca.bc.gov.educ.api.gradstudent.struct.School;
import ca.bc.gov.educ.api.gradstudent.transformer.SchoolTransformer;
import ca.bc.gov.educ.api.gradstudent.transformer.StudentTransformer;

@Service
public class GradStudentService {

    @Autowired
    GradStudentRepository gradStudentRepository;
    
    @Autowired
    StudentTransformer studentTransformer;
    
    @Autowired
    SchoolTransformer schoolTransformer;
    
    @Autowired
    SchoolRepository schoolRepository;

    public GradStudent getStudentByPen(String pen) {
    	GradStudent gradStudent = new GradStudent();
    	gradStudent = studentTransformer.transformToDTO(gradStudentRepository.findById(pen));
    	if(gradStudent != null) {
    		School school = schoolTransformer.transformToDTO(schoolRepository.findByMinCode(gradStudent.getMincode()));
    		if(school != null) {
    			gradStudent.setSchoolName(school.getSchoolName());
    		}
    	}
    	return gradStudent;
    }

	public List<GradStudent> getStudentByLastName(String lastName, Integer pageNo, Integer pageSize) {
		List<GradStudent> gradStudentList = new ArrayList<GradStudent>();
		Pageable paging = PageRequest.of(pageNo, pageSize);        	 
        Page<GradStudentEntity> pagedResult = gradStudentRepository.findByStudSurname(StringUtils.toRootUpperCase(lastName),paging);
		gradStudentList = studentTransformer.transformToDTO(pagedResult.getContent());
		gradStudentList.forEach(gS -> {
			if(gS != null) {
	    		School school = schoolTransformer.transformToDTO(schoolRepository.findByMinCode(gS.getMincode()));
	    		if(school != null) {
	    			gS.setSchoolName(school.getSchoolName());
	    		}
	    	}
		});			
    	return gradStudentList;
	}
}
