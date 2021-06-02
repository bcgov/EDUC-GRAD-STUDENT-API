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
import org.springframework.core.ParameterizedTypeReference;
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
import ca.bc.gov.educ.api.gradstudent.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.dto.GraduationStatus;
import ca.bc.gov.educ.api.gradstudent.dto.RestResponsePage;
import ca.bc.gov.educ.api.gradstudent.dto.School;
import ca.bc.gov.educ.api.gradstudent.dto.Search;
import ca.bc.gov.educ.api.gradstudent.dto.SearchCriteria;
import ca.bc.gov.educ.api.gradstudent.dto.Student;
import ca.bc.gov.educ.api.gradstudent.dto.StudentSearch;
import ca.bc.gov.educ.api.gradstudent.dto.ValueType;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;

@Service
public class GradStudentService {

	@Autowired
	EducGradStudentApiConstants constants;
    
    @Autowired
    WebClient webClient;
    
    @Autowired
    RestTemplate restTemplate;

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
			RestResponsePage<Student> response = restTemplate.exchange(String.format(constants.getPenStudentApiUrl(),pageNumber,pageSize,encodedURL), HttpMethod.GET,
    				new HttpEntity<>(httpHeaders), new ParameterizedTypeReference<RestResponsePage<Student>>() {}).getBody();
			List<Student> studentList = response.getContent();
			studentList.forEach(st-> {
				GradSearchStudent gradStu = new GradSearchStudent();
				BeanUtils.copyProperties(st, gradStu);
				ResponseEntity<GraduationStatus> responseEntity = restTemplate.exchange(String.format(constants.getGradStatusForStudentUrl(),st.getPen()), HttpMethod.GET,
						new HttpEntity<>(httpHeaders), GraduationStatus.class);
	    		if(responseEntity.getStatusCode().equals(HttpStatus.OK)) {
	    			gradStu.setProgram(responseEntity.getBody().getProgram());
	    			gradStu.setSchoolOfRecord(responseEntity.getBody().getSchoolOfRecord());
	    			gradStu.setStudentGrade(responseEntity.getBody().getStudentGrade());
	    			gradStu.setStudentStatus(responseEntity.getBody().getStudentStatus());
	    		}
	    		ResponseEntity<School> responseSchoolOfRecordEntity = restTemplate.exchange(String.format(constants.getSchoolByMincodeUrl(), gradStu.getSchoolOfRecord()), HttpMethod.GET,
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
    	List<Student> stuDataList = webClient.get().uri(String.format(constants.getPenStudentApiByPenUrl(), pen)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(new ParameterizedTypeReference<List<Student>>() {}).block();
    	stuDataList.forEach(st-> {
			GradSearchStudent gradStu = new GradSearchStudent();
			BeanUtils.copyProperties(st, gradStu);
			GraduationStatus gradObj  = webClient.get().uri(String.format(constants.getGradStatusForStudentUrl(),st.getStudentID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationStatus.class).block();
    		if(gradObj != null) {
    			gradStu.setProgram(gradObj.getProgram());
    			gradStu.setStudentGrade(gradObj.getStudentGrade());
    			gradStu.setStudentStatus(gradObj.getStudentStatus());
    			gradStu.setSchoolOfRecord(gradObj.getSchoolOfRecord());
    		}
    		School school = webClient.get().uri(String.format(constants.getSchoolByMincodeUrl(), gradStu.getSchoolOfRecord())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(School.class).block();
			if(school != null) {
    			gradStu.setSchoolOfRecordName(school.getSchoolName());
    			gradStu.setSchoolOfRecordindependentAffiliation(school.getIndependentAffiliation());
    		}
    		gradStudentList.add(gradStu);
    		
		});
    	
    	return gradStudentList;
    }
}
