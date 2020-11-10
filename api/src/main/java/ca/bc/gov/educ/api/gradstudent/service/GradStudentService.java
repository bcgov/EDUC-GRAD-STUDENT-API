package ca.bc.gov.educ.api.gradstudent.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradStudentRepository;
import ca.bc.gov.educ.api.gradstudent.repository.SchoolRepository;
import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;
import ca.bc.gov.educ.api.gradstudent.struct.School;
import ca.bc.gov.educ.api.gradstudent.transformer.SchoolTransformer;
import ca.bc.gov.educ.api.gradstudent.transformer.StudentTransformer;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;

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
    
    @Autowired
    RestTemplate restTemplate;    
    
    @Value(EducGradStudentApiConstants.ENDPOINT_SCHOOL_BY_MIN_CODE_URL)
    private String getSchoolByMinCodeURL;
    
    @Value("${spring.security.user.name}")
    private String uName;
    
    @Value("${spring.security.user.password}")
    private String pass;

    HttpHeaders httpHeaders = EducGradStudentApiUtils.getHeaders(uName, pass);
    
    public GradStudent getStudentByPen(String pen) {   	
    	GradStudent gradStudent = new GradStudent();
    	gradStudent = studentTransformer.transformToDTO(gradStudentRepository.findById(pen));
    	if(gradStudent != null) {
    		School schoolData = restTemplate.exchange(getSchoolByMinCodeURL.replace("{minCode}", gradStudent.getMincode()), HttpMethod.GET,
                    new HttpEntity<>(httpHeaders), School.class).getBody();
            if(schoolData != null) {
    			gradStudent.setSchoolName(schoolData.getSchoolName());
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
				School schoolData = restTemplate.exchange(getSchoolByMinCodeURL.replace("{minCode}", gS.getMincode()), HttpMethod.GET,
	                    new HttpEntity<>(httpHeaders), School.class).getBody();
	    		if(schoolData != null) {
	    			gS.setSchoolName(schoolData.getSchoolName());
	    		}
	    	}
		});			
    	return gradStudentList;
	}
}
