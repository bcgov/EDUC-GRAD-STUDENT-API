package ca.bc.gov.educ.api.gradstudent.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.gradstudent.dto.Condition;
import ca.bc.gov.educ.api.gradstudent.dto.FilterOperation;
import ca.bc.gov.educ.api.gradstudent.dto.GradCountry;
import ca.bc.gov.educ.api.gradstudent.dto.GradProvince;
import ca.bc.gov.educ.api.gradstudent.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.dto.GradStudent;
import ca.bc.gov.educ.api.gradstudent.dto.GraduationStatus;
import ca.bc.gov.educ.api.gradstudent.dto.RestResponsePage;
import ca.bc.gov.educ.api.gradstudent.dto.School;
import ca.bc.gov.educ.api.gradstudent.dto.Search;
import ca.bc.gov.educ.api.gradstudent.dto.SearchCriteria;
import ca.bc.gov.educ.api.gradstudent.dto.Student;
import ca.bc.gov.educ.api.gradstudent.dto.StudentSearch;
import ca.bc.gov.educ.api.gradstudent.dto.ValueType;
import ca.bc.gov.educ.api.gradstudent.entity.GradStudentEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradStudentRepository;
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
    WebClient webClient;
    
    @Autowired
    RestTemplate restTemplate;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_SCHOOL_BY_MIN_CODE_URL)
    private String getSchoolByMinCodeURL;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_COUNTRY_BY_COUNTRY_CODE_URL)
    private String getCountryByCountryCodeURL;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_PROVINCE_BY_PROV_CODE_URL)
    private String getProvinceByProvCodeURL;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_PEN_STUDENT_API_URL)
    private String getPenStudentAPIURL;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_PEN_STUDENT_API_BY_PEN_URL)
    private String getPenStudentAPIByPenURL;
    
    @Value(EducGradStudentApiConstants.ENDPOINT_GRAD_STUDENT_API_URL)
    private String getGradStatusForStudent;    
    
    @Transactional
    public GradStudent getStudentByPen(String pen, String accessToken) {
    	GradStudent gradStudent = studentTransformer.transformToDTO(gradStudentRepository.findById(pen));
    	if(gradStudent != null) {
    		School schoolData = webClient.get().uri(String.format(getSchoolByMinCodeURL, gradStudent.getMincode())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(School.class).block();
    		if(schoolData != null) {
    			gradStudent.setSchoolName(schoolData.getSchoolName());
    		}
    		GradCountry country = webClient.get().uri(String.format(getCountryByCountryCodeURL, gradStudent.getCountryCode())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GradCountry.class).block();
            if(country != null) {
    			gradStudent.setCountryName(country.getCountryName());
    		}
            GradProvince province = webClient.get().uri(String.format(getProvinceByProvCodeURL, gradStudent.getProvinceCode())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GradProvince.class).block();
            if(province != null) {
    			gradStudent.setProvinceName(province.getProvName());
    		}
    	}
    	return gradStudent;
    }

    @Transactional
	public List<GradStudent> getStudentByLastName(String lastName, Integer pageNo, Integer pageSize, String accessToken) {	
		Pageable paging = PageRequest.of(pageNo, pageSize);
		Page<GradStudentEntity> pagedResult = null;
		if(StringUtils.contains("*", lastName)) {
			pagedResult = gradStudentRepository.findByStudSurnameContaining(StringUtils.toRootUpperCase(StringUtils.strip(lastName, "*")),paging);
		}else {
			pagedResult = gradStudentRepository.findByStudSurname(StringUtils.toRootUpperCase(lastName),paging);
		}
				
    	return studentTransformer.transformToDTO(pagedResult.getContent());
	} 
    
    @Transactional
	public List<GradStudent> getStudentByFirstName(String firstName, Integer pageNo, Integer pageSize, String accessToken) {
		Pageable paging = PageRequest.of(pageNo, pageSize);
		Page<GradStudentEntity> pagedResult = null;
		if(StringUtils.contains("*", firstName)) {
			pagedResult = gradStudentRepository.findByStudGivenContaining(StringUtils.toRootUpperCase(StringUtils.strip(firstName, "*")),paging);
		}else {
			pagedResult = gradStudentRepository.findByStudGiven(StringUtils.toRootUpperCase(firstName),paging);
		}			
    	return studentTransformer.transformToDTO(pagedResult.getContent());
	}
    
    @Transactional
	public List<GradStudent> getStudentByLastNameAndFirstName(String lastName, String firstName,Integer pageNo, Integer pageSize, String accessToken) {
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
    	return studentTransformer.transformToDTO(pagedResult.getContent());		
	}
    
    @Transactional
	public List<GradStudent> getStudentByPens(List<String> penList, String accessToken) {
		return studentTransformer.transformToDTO(gradStudentRepository.findByPenList(penList));
	}

	public StudentSearch getStudentFromStudentAPI(String legalFirstName, String legalLastName, String legalMiddleNames,String usualFirstName, String usualLastName, String usualMiddleNames,
			String gender, String mincode, String localID, String birthdateFrom,String birthdateTo, Integer pageNumber, Integer pageSize, String accessToken) {
		HttpHeaders httpHeaders = EducGradStudentApiUtils.getHeaders(accessToken);
		List<GradSearchStudent> gradStudentList = new ArrayList<>();
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
			restTemplate.getMessageConverters().add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));
			MappingJackson2HttpMessageConverter mappingJackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
			mappingJackson2HttpMessageConverter.setSupportedMediaTypes(Arrays.asList(MediaType.APPLICATION_JSON, MediaType.APPLICATION_OCTET_STREAM));
			restTemplate.getMessageConverters().add(1, mappingJackson2HttpMessageConverter);
			RestResponsePage<Student> response = restTemplate.exchange(String.format(getPenStudentAPIURL,pageNumber,pageSize,encodedURL), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<RestResponsePage<Student>>() {}).getBody();
			List<Student> studentList = response.getContent();
			studentList.forEach(st-> {
				GradSearchStudent gradStu = new GradSearchStudent();
				BeanUtils.copyProperties(st, gradStu);
				ResponseEntity<GraduationStatus> responseEntity = restTemplate.exchange(String.format(getGradStatusForStudent,st.getStudentID()), HttpMethod.GET,
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
			e.getMessage();
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
					criteria = SearchCriteria.builder().key(paramterType).operation(FilterOperation.STARTS_WITH_IGNORE_CASE).value(StringUtils.strip(value,"*")).valueType(ValueType.STRING).condition(Condition.AND).build();
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
    	List<GradSearchStudent> gradStudentList = new ArrayList<>();
    	List<Student> stuDataList = webClient.get().uri(String.format(getPenStudentAPIByPenURL, pen)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(new ParameterizedTypeReference<List<Student>>() {}).block();
    	stuDataList.forEach(st-> {
			GradSearchStudent gradStu = new GradSearchStudent();
			BeanUtils.copyProperties(st, gradStu);
			GraduationStatus gradObj  = webClient.get().uri(String.format(getGradStatusForStudent,st.getStudentID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationStatus.class).block();
    		if(gradObj != null) {
    			gradStu.setProgram(gradObj.getProgram());
    			gradStu.setStudentGrade(gradObj.getStudentGrade());
    			gradStu.setStudentStatus(gradObj.getStudentStatus());
    			gradStu.setSchoolOfRecord(gradObj.getSchoolOfRecord());
    		}
    		School school = webClient.get().uri(String.format(getSchoolByMinCodeURL, gradStu.getSchoolOfRecord())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(School.class).block();
			if(school != null) {
    			gradStu.setSchoolOfRecordName(school.getSchoolName());
    			gradStu.setSchoolOfRecordindependentAffiliation(school.getIndependentAffiliation());
    		}
    		gradStudentList.add(gradStu);
    		
		});
    	
    	return gradStudentList;
    }
}
