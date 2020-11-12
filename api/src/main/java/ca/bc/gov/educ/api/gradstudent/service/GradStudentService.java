package ca.bc.gov.educ.api.gradstudent.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import ca.bc.gov.educ.api.gradstudent.repository.GradStudentRepository;
import ca.bc.gov.educ.api.gradstudent.struct.GradCountry;
import ca.bc.gov.educ.api.gradstudent.struct.GradProvince;
import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;
import ca.bc.gov.educ.api.gradstudent.struct.School;
import ca.bc.gov.educ.api.gradstudent.transformer.StudentTransformer;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;

@Service
public class GradStudentService {

    @Autowired
    GradStudentRepository gradStudentRepository;
    
    @Autowired
    StudentTransformer studentTransformer;
    
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
		Map<String,String> MAP_COUNTRY = getCountryList();
	    Map<String,String> MAP_PROVINCE = getProvinceList();
		Pageable paging = PageRequest.of(pageNo, pageSize);        	 
        Page<GradStudentEntity> pagedResult = gradStudentRepository.findByStudSurname(StringUtils.toRootUpperCase(lastName),paging);
		gradStudentList = studentTransformer.transformToDTO(pagedResult.getContent());
		gradStudentList.forEach(gS -> {
			if(gS != null) {
				School schoolData = restTemplate.getForObject(String.format(getSchoolByMinCodeURL, gS.getMincode()), School.class);
	    		if(schoolData != null) {
	    			gS.setSchoolName(schoolData.getSchoolName());
	    		}
	    		gS.setCountryName(MAP_COUNTRY.get(gS.getCountryCode()));
	    		gS.setProvinceName(MAP_PROVINCE.get(gS.getProvinceCode()));	            
	    	}
		});			
    	return gradStudentList;
	}
    
    public Map<String,String> getCountryList() {
		GradCountry[] countryArray = restTemplate.getForObject(getAllCountriesURL, GradCountry[].class);
		List<GradCountry> countryList =  Arrays.asList(countryArray);
		if(countryList.size() > 0) {
			Map<String,String> mapCounty = countryList.stream().collect(Collectors.toMap(GradCountry::getCountryCode, GradCountry::getCountryName));
			return mapCounty;
		}
		return null;		
	}

	public Map<String, String> getProvinceList() {
		List<GradProvince> provinceList =  Arrays.asList(restTemplate.getForObject(getAllProvincesURL, GradProvince[].class));
		if(provinceList.size() > 0) {
			Map<String,String> mapProvince = provinceList.stream().collect(Collectors.toMap(GradProvince::getProvCode, GradProvince::getProvName));
			return mapProvince;
		}
		return null;	
	}
}
