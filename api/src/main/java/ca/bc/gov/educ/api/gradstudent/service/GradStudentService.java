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
    		School schoolData = restTemplate.getForObject(String.format(getSchoolByMinCodeURL, gradStudent.getMincode()), School.class);
            if(schoolData != null) {
    			gradStudent.setSchoolName(schoolData.getSchoolName());
    		}
            
            GradCountry country = restTemplate.getForObject(String.format(getCountryByCountryCodeURL, gradStudent.getCountryCode()), GradCountry.class);
            if(country != null) {
    			gradStudent.setCountryName(country.getCountryName());
    		}
            
            GradProvince province = restTemplate.getForObject(String.format(getProvinceByProvCodeURL, gradStudent.getProvinceCode()), GradProvince.class);
            if(province != null) {
    			gradStudent.setProvinceName(province.getProvName());
    		}
    	}
    	return gradStudent;
    }

    @Transactional
	public List<GradStudent> getStudentByLastName(String lastName, Integer pageNo, Integer pageSize) {
		List<GradStudent> gradStudentList = new ArrayList<GradStudent>();		
		Pageable paging = PageRequest.of(pageNo, pageSize);
		Page<GradStudentEntity> pagedResult = null;
		if(StringUtils.contains("*", lastName)) {
			pagedResult = gradStudentRepository.findByStudSurnameContaining(StringUtils.toRootUpperCase(StringUtils.strip(lastName, "*")),paging);
		}else {
			pagedResult = gradStudentRepository.findByStudSurname(StringUtils.toRootUpperCase(lastName),paging);
		}
		gradStudentList = studentTransformer.transformToDTO(pagedResult.getContent());				
    	return gradStudentList;
	} 
    
    @Transactional
	public List<GradStudent> getStudentByFirstName(String firstName, Integer pageNo, Integer pageSize) {
		List<GradStudent> gradStudentList = new ArrayList<GradStudent>();		
		Pageable paging = PageRequest.of(pageNo, pageSize);
		Page<GradStudentEntity> pagedResult = null;
		if(StringUtils.contains("*", firstName)) {
			pagedResult = gradStudentRepository.findByStudGivenContaining(StringUtils.toRootUpperCase(StringUtils.strip(firstName, "*")),paging);
		}else {
			pagedResult = gradStudentRepository.findByStudGiven(StringUtils.toRootUpperCase(firstName),paging);
		}
		gradStudentList = studentTransformer.transformToDTO(pagedResult.getContent());				
    	return gradStudentList;
	}
    
    @Transactional
	public List<GradStudent> getStudentByLastNameAndFirstName(String lastName, String firstName,Integer pageNo, Integer pageSize) {
		List<GradStudent> gradStudentList = new ArrayList<GradStudent>();		
		Pageable paging = PageRequest.of(pageNo, pageSize);
		Page<GradStudentEntity> pagedResult = null;
		if(StringUtils.contains(lastName,"*") && StringUtils.contains(firstName,"*")) {
			pagedResult = gradStudentRepository.findByStudSurnameContainingAndStudGivenContaining(StringUtils.toRootUpperCase(StringUtils.strip(lastName, "*")),StringUtils.toRootUpperCase(StringUtils.strip(firstName, "*")),paging);
		}else {
			if(StringUtils.contains(firstName,"*") && !StringUtils.contains(lastName,"*")) {
				pagedResult = gradStudentRepository.findByStudSurnameAndStudGivenContaining(StringUtils.toRootUpperCase(StringUtils.strip(lastName, "*")),StringUtils.toRootUpperCase(StringUtils.strip(firstName, "*")),paging);
			}else if(!StringUtils.contains(firstName,"*") && StringUtils.contains(lastName,"*")) {
				pagedResult = gradStudentRepository.findByStudSurnameContainingAndStudGiven(StringUtils.toRootUpperCase(StringUtils.strip(lastName, "*")),StringUtils.toRootUpperCase(StringUtils.strip(firstName, "*")),paging);
			}else {
				pagedResult = gradStudentRepository.findByStudSurnameAndStudGiven(StringUtils.toRootUpperCase(lastName),StringUtils.toRootUpperCase(firstName),paging);
			}
		}
		gradStudentList = studentTransformer.transformToDTO(pagedResult.getContent());				
    	return gradStudentList;
	}
    
    @Transactional
	public List<GradStudent> getStudentByPens(List<String> penList) {
		return studentTransformer.transformToDTO(gradStudentRepository.findByPenList(penList));
	} 
    
}
