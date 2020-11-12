package ca.bc.gov.educ.api.gradstudent.service;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradCountryRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GradProvinceRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GradStudentRepository;
import ca.bc.gov.educ.api.gradstudent.repository.SchoolRepository;
import ca.bc.gov.educ.api.gradstudent.struct.GradCountry;
import ca.bc.gov.educ.api.gradstudent.struct.GradProvince;
import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;
import ca.bc.gov.educ.api.gradstudent.struct.School;
import ca.bc.gov.educ.api.gradstudent.transformer.GradCountryTransformer;
import ca.bc.gov.educ.api.gradstudent.transformer.GradProvinceTransformer;
import ca.bc.gov.educ.api.gradstudent.transformer.SchoolTransformer;
import ca.bc.gov.educ.api.gradstudent.transformer.StudentTransformer;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;

@Service
public class GradStudentService {

    @Autowired
    GradStudentRepository gradStudentRepository;
    
    @Autowired
    StudentTransformer studentTransformer;
    
    @Autowired
    SchoolRepository schoolRepository;
    
    @Autowired
    SchoolTransformer schoolTransformer;
    
    @Autowired
    GradCountryRepository gradCountryRepository;
    
    @Autowired
    GradCountryTransformer gradCountryTransformer;
    
    @Autowired
    GradProvinceRepository gradProvinceRepository;
    
    @Autowired
    GradProvinceTransformer gradProvinceTransformer;
    
    
    @Autowired
    RestTemplate restTemplate;
    
    @Autowired
    RestTemplateBuilder restTemplateBuilder;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_SCHOOL_BY_MIN_CODE_URL)
    private String getSchoolByMinCodeURL;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_COUNTRY_BY_COUNTRY_CODE_URL)
    private String getCountryByCountryCodeURL;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_PROVINCE_BY_PROV_CODE_URL)
    private String getProvinceByProvCodeURL;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_ALL_COUNTRY_URL)
    private String getAllCountriesURL;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_ALL_PROVINCE_URL)
    private String getAllProvincesURL;
    
    
    @Transactional
    public GradStudent getStudentByPen(String pen) {
    	GradStudent gradStudent = new GradStudent();
    	gradStudent = studentTransformer.transformToDTO(gradStudentRepository.findById(pen));
    	if(gradStudent != null) {
    		School school = schoolTransformer.transformToDTO(schoolRepository.findByMinCode(gradStudent.getMincode()));
    		if(school != null) {
    			gradStudent.setSchoolName(school.getSchoolName());
    		}
    		GradCountry gradCountry = gradCountryTransformer.transformToDTO(gradCountryRepository.findById(gradStudent.getCountryCode()));
    		if(gradCountry != null) {
    			gradStudent.setCountryName(gradCountry.getCountryName());
    		}
    		GradProvince gradProvince = gradProvinceTransformer.transformToDTO(gradProvinceRepository.findById(gradStudent.getProvinceCode()));
    		if(gradProvince != null) {
    			gradStudent.setProvinceName(gradProvince.getProvName());
    		}
    	}
    	return gradStudent;
    }

    @Transactional
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
	    		GradCountry gradCountry = gradCountryTransformer.transformToDTO(gradCountryRepository.findById(gS.getCountryCode()));
	    		if(gradCountry != null) {
	    			gS.setCountryName(gradCountry.getCountryName());
	    		}
	    		GradProvince gradProvince = gradProvinceTransformer.transformToDTO(gradProvinceRepository.findById(gS.getProvinceCode()));
	    		if(gradProvince != null) {
	    			gS.setProvinceName(gradProvince.getProvName());
	    		}	            
	    	}
		});			
    	return gradStudentList;
	}   
}
