package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.dto.*;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.transaction.Transactional;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Service
public class GradStudentService {

	private final EducGradStudentApiConstants constants;
    private final WebClient webClient;

    public GradStudentService(EducGradStudentApiConstants constants, WebClient webClient) {
    	this.constants = constants;
    	this.webClient = webClient;
	}

	public StudentSearch getStudentFromStudentAPI(String legalFirstName, String legalLastName, String legalMiddleNames,String usualFirstName, String usualLastName, String usualMiddleNames,
			String gender, String mincode, String localID, String birthdateFrom,String birthdateTo, Integer pageNumber, Integer pageSize, String accessToken) {
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
			RestResponsePage<Student> response = webClient.get().uri(constants.getPenStudentApiUrl(),
				uri -> uri
					.queryParam("pageNumber", pageNumber)
					.queryParam("pageSize", pageSize)
					.queryParam("searchCriteriaList", encodedURL)
				.build())
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(new ParameterizedTypeReference<RestResponsePage<Student>>() {}).block();
			List<Student> studentList = response.getContent();
			if (!studentList.isEmpty()) {
				studentList.forEach(st -> {
					GradSearchStudent gradStu = populateGradSearchStudent(st, accessToken);
					gradStudentList.add(gradStu);
				});
			}
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
			e.printStackTrace();
		}
		return null;
	}
	
	@Transactional
    public List<GradSearchStudent> getStudentByPenFromStudentAPI(String pen, String accessToken) {
    	List<GradSearchStudent> gradStudentList = new ArrayList<>();
    	List<Student> stuDataList = webClient.get().uri(String.format(constants.getPenStudentApiByPenUrl(), pen)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(new ParameterizedTypeReference<List<Student>>() {}).block();
    	if (stuDataList != null && !stuDataList.isEmpty()) {
			stuDataList.forEach(st -> {
				GradSearchStudent gradStu = populateGradSearchStudent(st, accessToken);
				gradStudentList.add(gradStu);
			});
		}
    	
    	return gradStudentList;
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

    private GradSearchStudent populateGradSearchStudent(Student student, String accessToken) {
		GradSearchStudent gradStu = new GradSearchStudent();
		BeanUtils.copyProperties(student, gradStu);
		GraduationStatus gradObj = webClient.get().uri(String.format(constants.getGradStatusForStudentUrl(), student.getStudentID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(GraduationStatus.class).block();
		if (gradObj != null) {
			gradStu.setProgram(gradObj.getProgram());
			gradStu.setStudentGrade(gradObj.getStudentGrade());
			gradStu.setStudentStatus(gradObj.getStudentStatus());
			gradStu.setSchoolOfRecord(gradObj.getSchoolOfRecord());
		}
		School school = webClient.get().uri(String.format(constants.getSchoolByMincodeUrl(), gradStu.getSchoolOfRecord())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(School.class).block();
		if (school != null) {
			gradStu.setSchoolOfRecordName(school.getSchoolName());
			gradStu.setSchoolOfRecordindependentAffiliation(school.getIndependentAffiliation());
		}
		return gradStu;
	}
}
