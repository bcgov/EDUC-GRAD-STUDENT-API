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
import ca.bc.gov.educ.api.gradstudent.struct.StudentSearch;
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
    
    @Value(EducGradStudentApiConstants.ENDPOINT_PEN_STUDENT_API_BY_PEN_URL)
    private String getPenStudentAPIByPenURL;
    
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

	public StudentSearch getStudentFromStudentAPI(String legalFirstName, String legalLastName, String legalMiddleNames,String usualFirstName, String usualLastName, String usualMiddleNames,
			String gender, String mincode, String localID, String birthdateFrom,String birthdateTo, Integer pageNumber, Integer pageSize, String accessToken) {
		HttpHeaders httpHeaders = EducGradStudentApiUtils.getHeaders(accessToken);
		List<GradSearchStudent> gradStudentList = new ArrayList<GradSearchStudent>();
		List<SearchCriteria> criteriaList = new ArrayList<>();
		criteriaList = getSearchCriteria(legalFirstName,null,"legalFirstName",criteriaList);
		criteriaList = getSearchCriteria(legalLastName,null,"legalLastName",criteriaList);
		criteriaList = getSearchCriteria(legalMiddleNames,null,"legalMiddleNames",criteriaList);
		criteriaList = getSearchCriteria(usualFirstName,null,"usualFirstName",criteriaList);
		criteriaList = getSearchCriteria(usualLastName,null,"usualLastName",criteriaList);
		criteriaList = getSearchCriteria(usualMiddleNames,null,"usualMiddleNames",criteriaList);
		criteriaList = getSearchCriteria(localID,null,"localID",criteriaList);
		criteriaList = getSearchCriteria(gender,null,"genderCode",criteriaList);
		criteriaList = getSearchCriteria(birthdateFrom,birthdateTo,"dob",criteriaList);
		criteriaList = getSearchCriteria(mincode,null,"mincode",criteriaList);
		
		List<Search> searches = new LinkedList<>();
		StudentSearch searchObj = new StudentSearch();
	    searches.add(Search.builder().condition(Condition.AND).searchCriteriaList(criteriaList).build());
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
			String criteriaJSON = objectMapper.writeValueAsString(searches);
			String encodedURL = URLEncoder.encode(criteriaJSON,StandardCharsets.UTF_8.toString());
			DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
		    defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
		    restTemplate.setUriTemplateHandler(defaultUriBuilderFactory);
			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));
			RestResponsePage<Student> response = restTemplate.exchange(String.format(getPenStudentAPIURL,pageNumber,pageSize,encodedURL), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<RestResponsePage<Student>>() {}).getBody();
			List<Student> studentList = response.getContent();
			studentList.forEach(st-> {
				GradSearchStudent gradStu = new GradSearchStudent();
				BeanUtils.copyProperties(st, gradStu);
				ResponseEntity<GraduationStatus> responseEntity = restTemplate.exchange(String.format(getGradStatusForStudent,st.getPen()), HttpMethod.GET,
						new HttpEntity<>(httpHeaders), GraduationStatus.class);
	    		if(responseEntity.getStatusCode().equals(HttpStatus.OK)) {
	    			gradStu.setProgram(responseEntity.getBody().getProgram());
	    			gradStu.setSchoolOfRecord(responseEntity.getBody().getSchoolOfRecord());
	    			gradStu.setStudentGrade(responseEntity.getBody().getStudentGrade());
	    			gradStu.setStudentStatus(responseEntity.getBody().getStudentStatus());
	    		}
	    		ResponseEntity<School> responseSchoolOfRecordEntity = restTemplate.exchange(String.format(getSchoolByMinCodeURL, gradStu.getSchoolOfRecord()), HttpMethod.GET,
	    				new HttpEntity<>(httpHeaders), School.class);
				if(responseSchoolOfRecordEntity.getStatusCode().equals(HttpStatus.OK)) {
	    			gradStu.setSchoolOfRecordName(responseSchoolOfRecordEntity.getBody().getSchoolName());
	    			gradStu.setSchoolOfRecordindependentAffiliation(responseSchoolOfRecordEntity.getBody().getIndependentAffiliation());
	    		}
	    		gradStudentList.add(gradStu);
	    		
			});
			searchObj.setGradSearchStudents(gradStudentList);
			searchObj.setPageable(response.getPageable());
			searchObj.setTotalElements(response.getTotalElements());
			searchObj.setTotalPages(response.getTotalPages());
			searchObj.setSize(response.getSize());
			searchObj.setNumberOfElements(response.getNumberOfElements());
			searchObj.setSort(response.getSort());
			searchObj.setNumber(response.getNumber());
			
			return searchObj;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}    
	
	private List<SearchCriteria> getSearchCriteria(String value,String value2,String paramterType,List<SearchCriteria> criteriaList) {
		SearchCriteria criteria = null;
		if(paramterType.equalsIgnoreCase("dob")) {
			if(StringUtils.isNotBlank(value) && StringUtils.isNotBlank(value2)) {
				criteria = SearchCriteria.builder().condition(Condition.AND).key("dob").operation(FilterOperation.BETWEEN).value(value + "," + value2).valueType(ValueType.DATE).build();
			}
		}else {
			if(StringUtils.isNotBlank(value)) {
				if(StringUtils.contains(value,"*")) {
					criteria = SearchCriteria.builder().key(paramterType).operation(FilterOperation.CONTAINS).value(StringUtils.strip(value,"*")).valueType(ValueType.STRING).condition(Condition.AND).build();
				}else {
					criteria = SearchCriteria.builder().key(paramterType).operation(FilterOperation.EQUAL).value(value).valueType(ValueType.STRING).condition(Condition.AND).build();
				}
			}
		}
		if(criteria != null) criteriaList.add(criteria);
		return criteriaList;
	}
	
	 @Transactional
    public List<GradSearchStudent> getStudentByPenFromStudentAPI(String pen, String accessToken) {
    	List<GradSearchStudent> gradStudentList = new ArrayList<GradSearchStudent>();
    	HttpHeaders httpHeaders = EducGradStudentApiUtils.getHeaders(accessToken);
    	List<Student> stuDataList = restTemplate.exchange(String.format(getPenStudentAPIByPenURL, pen), HttpMethod.GET,
				new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<List<Student>>() {}).getBody();
    	stuDataList.forEach(st-> {
			GradSearchStudent gradStu = new GradSearchStudent();
			BeanUtils.copyProperties(st, gradStu);
			ResponseEntity<School> responseSchoolEntity = restTemplate.exchange(String.format(getSchoolByMinCodeURL, st.getMincode()), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), School.class);
			if(responseSchoolEntity.getStatusCode().equals(HttpStatus.OK)) {
    			gradStu.setSchoolName(responseSchoolEntity.getBody().getSchoolName());
    			gradStu.setIndependentAffiliation(responseSchoolEntity.getBody().getIndependentAffiliation());
    		}
    		ResponseEntity<GraduationStatus> responseEntity = restTemplate.exchange(String.format(getGradStatusForStudent,st.getPen()), HttpMethod.GET,
					new HttpEntity<>(httpHeaders), GraduationStatus.class);
    		if(responseEntity.getStatusCode().equals(HttpStatus.OK)) {
    			gradStu.setProgram(responseEntity.getBody().getProgram());
    			gradStu.setStudentGrade(responseEntity.getBody().getStudentGrade());
    			gradStu.setStudentStatus(responseEntity.getBody().getStudentStatus());
    			gradStu.setSchoolOfRecord(responseEntity.getBody().getSchoolOfRecord());
    		}
    		ResponseEntity<School> responseSchoolOfRecordEntity = restTemplate.exchange(String.format(getSchoolByMinCodeURL, gradStu.getSchoolOfRecord()), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), School.class);
			if(responseSchoolOfRecordEntity.getStatusCode().equals(HttpStatus.OK)) {
    			gradStu.setSchoolOfRecordName(responseSchoolOfRecordEntity.getBody().getSchoolName());
    			gradStu.setSchoolOfRecordindependentAffiliation(responseSchoolOfRecordEntity.getBody().getIndependentAffiliation());
    		}
    		gradStudentList.add(gradStu);
    		
		});
    	
    	return gradStudentList;
    }
}
