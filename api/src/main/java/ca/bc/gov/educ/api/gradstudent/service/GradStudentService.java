package ca.bc.gov.educ.api.gradstudent.service;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradCountryRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GradProvinceRepository;
import ca.bc.gov.educ.api.gradstudent.repository.GradStudentRepository;
import ca.bc.gov.educ.api.gradstudent.repository.SchoolRepository;
import ca.bc.gov.educ.api.gradstudent.struct.Condition;
import ca.bc.gov.educ.api.gradstudent.struct.FilterOperation;
import ca.bc.gov.educ.api.gradstudent.struct.GradCountry;
import ca.bc.gov.educ.api.gradstudent.struct.GradProvince;
import ca.bc.gov.educ.api.gradstudent.struct.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;
import ca.bc.gov.educ.api.gradstudent.struct.GraduationStatus;
import ca.bc.gov.educ.api.gradstudent.struct.RestResponsePage;
import ca.bc.gov.educ.api.gradstudent.struct.School;
import ca.bc.gov.educ.api.gradstudent.struct.Search;
import ca.bc.gov.educ.api.gradstudent.struct.SearchCriteria;
import ca.bc.gov.educ.api.gradstudent.struct.Student;
import ca.bc.gov.educ.api.gradstudent.struct.ValueType;
import ca.bc.gov.educ.api.gradstudent.transformer.GradCountryTransformer;
import ca.bc.gov.educ.api.gradstudent.transformer.GradProvinceTransformer;
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
    
    @Value(EducGradStudentApiConstants.ENDPOINT_PEN_STUDENT_API_URL)
    private String getPenStudentAPIURL;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_GRAD_STUDENT_API_URL)
    private String getGradStatusForStudent;    
    
    @Transactional
    public GradStudent getStudentByPen(String pen, String accessToken) {
    	GradStudent gradStudent = new GradStudent();
    	gradStudent = studentTransformer.transformToDTO(gradStudentRepository.findById(pen));
    	HttpHeaders httpHeaders = EducGradStudentApiUtils.getHeaders(accessToken);
    	if(gradStudent != null) {
    		School schoolData = restTemplate.exchange(String.format(getSchoolByMinCodeURL, gradStudent.getMincode()), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), School.class).getBody();
    		if(schoolData != null) {
    			gradStudent.setSchoolName(schoolData.getSchoolName());
    		}
            GradCountry country = restTemplate.exchange(String.format(getCountryByCountryCodeURL, gradStudent.getCountryCode()), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), GradCountry.class).getBody();
            if(country != null) {
    			gradStudent.setCountryName(country.getCountryName());
    		}
            GradProvince province = restTemplate.exchange(String.format(getProvinceByProvCodeURL, gradStudent.getProvinceCode()), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), GradProvince.class).getBody();
            if(province != null) {
    			gradStudent.setProvinceName(province.getProvName());
    		}
    	}
    	return gradStudent;
    }

    @Transactional
	public List<GradStudent> getStudentByLastName(String lastName, Integer pageNo, Integer pageSize, String accessToken) {
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
	public List<GradStudent> getStudentByFirstName(String firstName, Integer pageNo, Integer pageSize, String accessToken) {
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
	public List<GradStudent> getStudentByLastNameAndFirstName(String lastName, String firstName,Integer pageNo, Integer pageSize, String accessToken) {
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
	public List<GradStudent> getStudentByPens(List<String> penList, String accessToken) {
		return studentTransformer.transformToDTO(gradStudentRepository.findByPenList(penList));
	}

	public List<GradSearchStudent> getStudentFromStudentAPI(String legalFistName, String legalLastName, String legalMiddleNames,String usualFistName, String usualLastName, String usualMiddleNames,
			String postalCode,String gender, String grade, String mincode, String localID, String birthdate, String accessToken) {
		HttpHeaders httpHeaders = EducGradStudentApiUtils.getHeaders(accessToken);
		List<GradSearchStudent> gradStudentList = new ArrayList<GradSearchStudent>();
		SearchCriteria criteriaLegalFirstName = null;
		SearchCriteria criteriaLegalLastName = null;
		SearchCriteria criteriaLegalMiddleName = null;
		SearchCriteria criteriaUsualFirstName = null;
		SearchCriteria criteriaUsualLastName = null;
		SearchCriteria criteriaUsualMiddleName = null;
		SearchCriteria criteriaPostalCode=null;
		SearchCriteria criteriaGender=null;
		SearchCriteria criteriaDob = null;
		SearchCriteria criteriaGrade=null;
		SearchCriteria criteriaMincode=null;
		if(StringUtils.isNotBlank(legalFistName)) {
			if(StringUtils.contains(legalFistName,"*")) {
				criteriaLegalFirstName = SearchCriteria.builder().key("legalFirstName").operation(FilterOperation.CONTAINS).value(StringUtils.strip(legalFistName,"*")).valueType(ValueType.STRING).condition(Condition.AND).build();
			}else {
				criteriaLegalFirstName = SearchCriteria.builder().key("legalFirstName").operation(FilterOperation.EQUAL).value(legalFistName).valueType(ValueType.STRING).condition(Condition.AND).build();
			}
		}
		if(StringUtils.isNotBlank(legalLastName)) {
			if(StringUtils.contains(legalLastName,"*")) {
				criteriaLegalLastName = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.CONTAINS).value(StringUtils.strip(legalLastName,"*")).valueType(ValueType.STRING).condition(Condition.AND).build();
			}else {
				criteriaLegalLastName = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.EQUAL).value(legalLastName).valueType(ValueType.STRING).condition(Condition.AND).build();
			}
		}
		if(StringUtils.isNotBlank(legalMiddleNames)) {
			if(StringUtils.contains(legalMiddleNames,"*")) {
				criteriaLegalMiddleName = SearchCriteria.builder().key("legalMiddleNames").operation(FilterOperation.CONTAINS).value(StringUtils.strip(legalMiddleNames,"*")).valueType(ValueType.STRING).condition(Condition.AND).build();
			}else {
				criteriaLegalMiddleName = SearchCriteria.builder().key("legalMiddleNames").operation(FilterOperation.EQUAL).value(legalMiddleNames).valueType(ValueType.STRING).condition(Condition.AND).build();
			}
		}
		if(StringUtils.isNotBlank(usualFistName)) {
			if(StringUtils.contains(usualFistName,"*")) {
				criteriaUsualFirstName = SearchCriteria.builder().key("usualFirstName").operation(FilterOperation.CONTAINS).value(StringUtils.strip(usualFistName,"*")).valueType(ValueType.STRING).condition(Condition.AND).build();
			}else {
				criteriaUsualFirstName = SearchCriteria.builder().key("usualFirstName").operation(FilterOperation.EQUAL).value(usualFistName).valueType(ValueType.STRING).condition(Condition.AND).build();
			}
		}
		if(StringUtils.isNotBlank(usualLastName)) {
			if(StringUtils.contains(usualLastName,"*")) {
				criteriaUsualLastName = SearchCriteria.builder().key("usualLastName").operation(FilterOperation.CONTAINS).value(StringUtils.strip(usualLastName,"*")).valueType(ValueType.STRING).condition(Condition.AND).build();
			}else {
				criteriaUsualLastName = SearchCriteria.builder().key("usualLastName").operation(FilterOperation.EQUAL).value(usualLastName).valueType(ValueType.STRING).condition(Condition.AND).build();
			}
		}
		if(StringUtils.isNotBlank(usualMiddleNames)) {
			if(StringUtils.contains(usualMiddleNames,"*")) {
				criteriaUsualMiddleName = SearchCriteria.builder().key("usualMiddleNames").operation(FilterOperation.CONTAINS).value(StringUtils.strip(usualMiddleNames,"*")).valueType(ValueType.STRING).condition(Condition.AND).build();
			}else {
				criteriaUsualMiddleName = SearchCriteria.builder().key("usualMiddleNames").operation(FilterOperation.EQUAL).value(usualMiddleNames).valueType(ValueType.STRING).condition(Condition.AND).build();
			}
		}
		if(StringUtils.isNotBlank(postalCode)) {
			criteriaPostalCode = SearchCriteria.builder().condition(Condition.AND).key("postalCode").operation(FilterOperation.EQUAL).value(postalCode).valueType(ValueType.STRING).build();
		}
		
		if(StringUtils.isNotBlank(gender)) {
			criteriaGender = SearchCriteria.builder().condition(Condition.AND).key("genderCode").operation(FilterOperation.EQUAL).value(gender).valueType(ValueType.STRING).build();
		}
		if(StringUtils.isNotBlank(grade)) {
			criteriaGrade = SearchCriteria.builder().condition(Condition.AND).key("gradeCode").operation(FilterOperation.EQUAL).value(grade).valueType(ValueType.STRING).build();
		}
		if(StringUtils.isNotBlank(mincode)) {
			criteriaMincode = SearchCriteria.builder().condition(Condition.AND).key("mincode").operation(FilterOperation.CONTAINS).value(mincode).valueType(ValueType.STRING).build();
		}
		if(StringUtils.isNotBlank(birthdate)) {
			criteriaDob = SearchCriteria.builder().condition(Condition.AND).key("dob").operation(FilterOperation.EQUAL).value(birthdate).valueType(ValueType.DATE).build();
		}  
		List<SearchCriteria> criteriaList = new ArrayList<>();
		if(criteriaLegalFirstName!= null)criteriaList.add(criteriaLegalFirstName);
		if(criteriaLegalLastName!= null)criteriaList.add(criteriaLegalLastName);
		if(criteriaLegalMiddleName!= null)criteriaList.add(criteriaLegalMiddleName);
		if(criteriaUsualFirstName!= null)criteriaList.add(criteriaUsualFirstName);
		if(criteriaUsualLastName!= null)criteriaList.add(criteriaUsualLastName);
		if(criteriaUsualMiddleName!= null)criteriaList.add(criteriaUsualMiddleName);
		if(criteriaPostalCode!= null)criteriaList.add(criteriaPostalCode);
		if(criteriaGender!= null)criteriaList.add(criteriaGender);
		if(criteriaGrade!= null)criteriaList.add(criteriaGrade);
		if(criteriaMincode!= null)criteriaList.add(criteriaMincode);
		if(criteriaDob!= null)criteriaList.add(criteriaDob);
		List<Search> searches = new LinkedList<>();
	    searches.add(Search.builder().condition(Condition.AND).searchCriteriaList(criteriaList).build());
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
			String criteriaJSON = objectMapper.writeValueAsString(searches);
			String encodedURL = URLEncoder.encode(criteriaJSON,StandardCharsets.UTF_8.toString());
			DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
		    defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
		    restTemplate.setUriTemplateHandler(defaultUriBuilderFactory);
			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			RestResponsePage<Student> response = restTemplate.exchange(String.format(getPenStudentAPIURL, encodedURL), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<RestResponsePage<Student>>() {}).getBody();
			List<Student> studentList = response.getContent();
			studentList.forEach(st-> {
				GradSearchStudent gradStu = new GradSearchStudent();
				BeanUtils.copyProperties(st, gradStu);
				ResponseEntity<School> responseSchoolEntity = restTemplate.exchange(String.format(getSchoolByMinCodeURL, st.getMincode()), HttpMethod.GET,
	    				new HttpEntity<>(httpHeaders), School.class);
				if(responseSchoolEntity.getStatusCode().equals(HttpStatus.OK)) {
	    			gradStu.setSchoolName(responseSchoolEntity.getBody().getSchoolName());
	    		}
	    		ResponseEntity<GraduationStatus> responseEntity = restTemplate.exchange(String.format(getGradStatusForStudent,st.getPen()), HttpMethod.GET,
						new HttpEntity<>(httpHeaders), GraduationStatus.class);
	    		if(responseEntity.getStatusCode().equals(HttpStatus.OK)) {
	    			gradStu.setProgram(responseEntity.getBody().getProgram());
	    		}
	    		gradStudentList.add(gradStu);
	    		
			});
			return gradStudentList;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}    
}
