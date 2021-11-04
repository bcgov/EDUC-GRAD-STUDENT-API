package ca.bc.gov.educ.api.gradstudent.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import io.github.resilience4j.retry.annotation.Retry;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.Condition;
import ca.bc.gov.educ.api.gradstudent.model.dto.FilterOperation;
import ca.bc.gov.educ.api.gradstudent.model.dto.GradOnlyStudentSearch;
import ca.bc.gov.educ.api.gradstudent.model.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.RestResponsePage;
import ca.bc.gov.educ.api.gradstudent.model.dto.School;
import ca.bc.gov.educ.api.gradstudent.model.dto.Search;
import ca.bc.gov.educ.api.gradstudent.model.dto.SearchCriteria;
import ca.bc.gov.educ.api.gradstudent.model.dto.Student;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentSearch;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValueType;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;

@Service
public class GradStudentService {

	private static final Logger logger = LoggerFactory.getLogger(GradStudentService.class);
	
	@Autowired EducGradStudentApiConstants constants;
    @Autowired WebClient webClient;
    @Autowired GraduationStudentRecordRepository graduationStatusRepository;
    @Autowired GraduationStatusTransformer graduationStatusTransformer;
    
    @Value("${more.search.match.found}")
	String messageStringMoreMatchesFound;
    
    private static final String LEGAL_FIRST_NAME="legalFirstName";
    private static final String LEGAL_LAST_NAME="legalLastName";
    private static final String LEGAL_MIDDLE_NAME="legalMiddleNames";
    private static final String USUAL_FIRST_NAME="usualFirstName";
    private static final String USUAL_LAST_NAME="usualLastName";
    private static final String USUAL_MIDDLE_NAME="usualMiddleNames";
    private static final String LOCAL_ID="localID";
    private static final String GENDER_CODE="genderCode";
    private static final String DOB="dob";
    private static final String MINCODE="mincode";
    private static final String PAGE_NUMBER="pageNumber";
    private static final String PAGE_SIZE="pageSize";
    private static final String SEARCH_CRITERIA_LIST = "searchCriteriaList";

	public StudentSearch getStudentFromStudentAPI(String legalFirstName, String legalLastName, String legalMiddleNames,String usualFirstName, String usualLastName, String usualMiddleNames,
			String gender, String mincode, String localID, String birthdateFrom,String birthdateTo, Integer pageNumber, Integer pageSize, String accessToken) {
		List<GradSearchStudent> gradStudentList = new ArrayList<>();
		List<SearchCriteria> criteriaList = new ArrayList<>();
		getSearchCriteria(legalFirstName,null,LEGAL_FIRST_NAME,criteriaList);
		getSearchCriteria(legalLastName,null,LEGAL_LAST_NAME,criteriaList);
		getSearchCriteria(legalMiddleNames,null,LEGAL_MIDDLE_NAME,criteriaList);
		getSearchCriteria(usualFirstName,null,USUAL_FIRST_NAME,criteriaList);
		getSearchCriteria(usualLastName,null,USUAL_LAST_NAME,criteriaList);
		getSearchCriteria(usualMiddleNames,null,USUAL_MIDDLE_NAME,criteriaList);
		getSearchCriteria(localID,null,LOCAL_ID,criteriaList);
		getSearchCriteria(gender,null,GENDER_CODE,criteriaList);
		getSearchCriteria(birthdateFrom,birthdateTo,DOB,criteriaList);
		getSearchCriteria(mincode,null,MINCODE,criteriaList);
		
		List<Search> searches = new LinkedList<>();
		StudentSearch searchObj = new StudentSearch();
	    searches.add(Search.builder().condition(Condition.AND).searchCriteriaList(criteriaList).build());
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
			String criteriaJSON = objectMapper.writeValueAsString(searches);
			String encodedURL = URLEncoder.encode(criteriaJSON,StandardCharsets.UTF_8.toString());
			RestResponsePage<Student> response = webClient.get().uri(constants.getPenStudentApiUrl(),
				uri -> uri
					.queryParam(PAGE_NUMBER, pageNumber)
					.queryParam(PAGE_SIZE, pageSize)
					.queryParam(SEARCH_CRITERIA_LIST, encodedURL)
				.build())
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(new ParameterizedTypeReference<RestResponsePage<Student>>() {}).block();
			List<Student> studentList = response != null ? response.getContent():new ArrayList<>();
			if (!studentList.isEmpty()) {
				studentList.forEach(st -> {
					GradSearchStudent gradStu = populateGradSearchStudent(st, accessToken);
					gradStudentList.add(gradStu);
				});
			}
			searchObj.setGradSearchStudents(gradStudentList);
			if(response != null) {
				searchObj.setPageable(response.getPageable());
				searchObj.setTotalElements(response.getTotalElements());
				searchObj.setTotalPages(response.getTotalPages());
				searchObj.setSize(response.getSize());
				searchObj.setNumberOfElements(response.getNumberOfElements());
				searchObj.setSort(response.getSort());
				searchObj.setNumber(response.getNumber());
			}
			
			return searchObj;
			
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return null;
	}
	
	public GradOnlyStudentSearch getGRADStudents(String legalFirstName, String legalLastName, String legalMiddleNames,String usualFirstName, String usualLastName, String usualMiddleNames,
			String gender, String mincode, String localID, String birthdateFrom,String birthdateTo,String schoolOfRecord, String gradProgram,Integer pageNumber,Integer pageSize,String accessToken) {
		
		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<GradSearchStudent> gradStudentList = new ArrayList<>();
		List<SearchCriteria> criteriaList = new ArrayList<>();
		ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
		Example<GraduationStudentRecordEntity> exampleQuery = Example.of(new GraduationStudentRecordEntity(gradProgram,schoolOfRecord), matcher);
		Page<GraduationStudentRecordEntity> pagedResult = graduationStatusRepository.findAll(exampleQuery,paging);
		List<GraduationStudentRecordEntity> studList = pagedResult.getContent();
		if(!studList.isEmpty()) {
			String studentIds = studList.stream().map(std -> String.valueOf(std.getStudentID())).collect(Collectors.joining(","));
			getSearchCriteria(studentIds,null,"studentID",criteriaList);
		}		
		getSearchCriteria(legalFirstName,null,LEGAL_FIRST_NAME,criteriaList);
		getSearchCriteria(legalLastName,null,LEGAL_LAST_NAME,criteriaList);
		getSearchCriteria(legalMiddleNames,null,LEGAL_MIDDLE_NAME,criteriaList);
		getSearchCriteria(usualFirstName,null,USUAL_FIRST_NAME,criteriaList);
		getSearchCriteria(usualLastName,null,USUAL_LAST_NAME,criteriaList);
		getSearchCriteria(usualMiddleNames,null,USUAL_MIDDLE_NAME,criteriaList);
		getSearchCriteria(localID,null,LOCAL_ID,criteriaList);
		getSearchCriteria(gender,null,GENDER_CODE,criteriaList);
		getSearchCriteria(birthdateFrom,birthdateTo,DOB,criteriaList);
		getSearchCriteria(mincode,null,MINCODE,criteriaList);
		
		List<Search> searches = new LinkedList<>();
		GradOnlyStudentSearch searchObj = new GradOnlyStudentSearch();
	    searches.add(Search.builder().condition(Condition.AND).searchCriteriaList(criteriaList).build());
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
			String criteriaJSON = objectMapper.writeValueAsString(searches);
			String encodedURL = URLEncoder.encode(criteriaJSON,StandardCharsets.UTF_8.toString());
			RestResponsePage<Student> response = webClient.get().uri(constants.getPenStudentApiUrl(),
				uri -> uri
					.queryParam(PAGE_NUMBER, "0")
					.queryParam(PAGE_SIZE, studList.size())
					.queryParam(SEARCH_CRITERIA_LIST, encodedURL)
				.build())
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(new ParameterizedTypeReference<RestResponsePage<Student>>() {}).block();
			List<Student> studentList = response != null ?response.getContent():new ArrayList<>();
			if (!studentList.isEmpty()) {
				studentList.forEach(st -> {
					GradSearchStudent gradStu = populateGradSearchStudent(st, accessToken);
					if(gradStu.getProgram() != null) {
						gradStudentList.add(gradStu);
					}
				});
			}
			searchObj.setGradSearchStudents(gradStudentList);
			searchObj.setPageable(pagedResult.getPageable());
			searchObj.setTotalElements(pagedResult.getTotalElements());
			searchObj.setTotalPages(pagedResult.getTotalPages());
			searchObj.setSize(pagedResult.getSize());
			searchObj.setNumberOfElements(pagedResult.getNumberOfElements());
			searchObj.setSort(pagedResult.getSort());
			searchObj.setNumber(pagedResult.getNumber());
			return searchObj;
	    } catch (Exception e) {
	    	logger.info(e.getMessage());
		}
		
	    return null;
	}

	@Transactional
	@Retry(name = "advancedsearch")
	public GradOnlyStudentSearch getStudentFromStudentAPIGradOnly(String legalFirstName, String legalLastName, String legalMiddleNames,String usualFirstName, String usualLastName, String usualMiddleNames,
			String gender, String minCode, String localID, String birthdateFrom,String birthdateTo,String accessToken) {
		List<GradSearchStudent> gradStudentList = new ArrayList<>();
		List<SearchCriteria> criteriaList = new ArrayList<>();
		getSearchCriteria(legalFirstName,null,LEGAL_FIRST_NAME,criteriaList);
		getSearchCriteria(legalLastName,null,LEGAL_LAST_NAME,criteriaList);
		getSearchCriteria(legalMiddleNames,null,LEGAL_MIDDLE_NAME,criteriaList);
		getSearchCriteria(usualFirstName,null,USUAL_FIRST_NAME,criteriaList);
		getSearchCriteria(usualLastName,null,USUAL_LAST_NAME,criteriaList);
		getSearchCriteria(usualMiddleNames,null,USUAL_MIDDLE_NAME,criteriaList);
		getSearchCriteria(localID,null,LOCAL_ID,criteriaList);
		getSearchCriteria(gender,null,GENDER_CODE,criteriaList);
		getSearchCriteria(birthdateFrom,birthdateTo,DOB,criteriaList);
		getSearchCriteria(minCode,null,MINCODE,criteriaList);
		
		List<Search> searches = new LinkedList<>();
		GradOnlyStudentSearch searchObj = new GradOnlyStudentSearch();
	    searches.add(Search.builder().condition(Condition.AND).searchCriteriaList(criteriaList).build());
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
			String criteriaJSON = objectMapper.writeValueAsString(searches);
			String encodedURL = URLEncoder.encode(criteriaJSON,StandardCharsets.UTF_8.toString());
			RestResponsePage<Student> response = webClient.get().uri(constants.getPenStudentApiUrl(),
				uri -> uri
					.queryParam(PAGE_NUMBER, "0")
					.queryParam(PAGE_SIZE, "50000")
					.queryParam(SEARCH_CRITERIA_LIST, encodedURL)
				.build())
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(new ParameterizedTypeReference<RestResponsePage<Student>>() {}).block();
			List<Student> studentLists = response != null ? response.getContent():new ArrayList<>();
			List<UUID> studentIds = studentLists.stream().map(std -> UUID.fromString(std.getStudentID())).collect(Collectors.toList());
			int partitionSize = 1000;
			List<List<UUID>> partitions = new LinkedList<>();
			for (int i = 0; i < studentIds.size(); i += partitionSize) {
				partitions.add(studentIds.subList(i,Math.min(i + partitionSize, studentIds.size())));
			}
			logger.debug(" partitions length " + partitions.size());
			for(int i=0; i<partitions.size();i++) {
				List<UUID> subList = partitions.get(i);
				logger.debug(" sub list length "+subList.size()+" par "+i);
				List<GraduationStudentRecordEntity> gradList = graduationStatusRepository.findByStudentIDIn(subList);
				if (!gradList.isEmpty()) {
					gradList.forEach(st -> {
						GradSearchStudent gradStu = populateGradStudent(st, accessToken);
						if(gradStu.getProgram() != null) {
							gradStudentList.add(gradStu);
						}
					});
				}
			}
			searchObj.setGradSearchStudents(gradStudentList);
			searchObj.setSearchMessage(String.format(messageStringMoreMatchesFound, response != null ? (response.getTotalElements()-gradStudentList.size()):0));
			
			return searchObj;
			
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return null;
	}
	
	@Transactional
	@Retry(name = "searchbypen")
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

	private void getSearchCriteria(String value, String value2, String paramterType, List<SearchCriteria> criteriaList) {
		SearchCriteria criteria = null;
		if(paramterType.equalsIgnoreCase(DOB)) {
			if(StringUtils.isNotBlank(value) && StringUtils.isNotBlank(value2)) {
				criteria = SearchCriteria.builder().condition(Condition.AND).key(paramterType).operation(FilterOperation.BETWEEN).value(value + "," + value2).valueType(ValueType.DATE).build();
			}
		}else if (paramterType.equalsIgnoreCase("studentID")) {
			criteria = SearchCriteria.builder().key(paramterType).operation(FilterOperation.IN).value(value).valueType(ValueType.UUID).condition(Condition.AND).build();
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
	}

	private GradSearchStudent populateGradStudent(GraduationStudentRecordEntity gradRecord, String accessToken) {
		GradSearchStudent gradStu = new GradSearchStudent();
		BeanUtils.copyProperties(gradRecord, gradStu);
		Student studentPen = webClient.get().uri(String.format(constants.getPenStudentApiByStudentIdUrl(), gradRecord.getStudentID())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(Student.class).block();
		if(studentPen != null) {
			BeanUtils.copyProperties(studentPen, gradStu);
		}
		School school = webClient.get().uri(String.format(constants.getSchoolByMincodeUrl(), gradStu.getSchoolOfRecord())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(School.class).block();
		if (school != null) {
			gradStu.setSchoolOfRecordName(school.getSchoolName());
			gradStu.setSchoolOfRecordindependentAffiliation(school.getIndependentAffiliation());
		}
		return gradStu;
	}

    private GradSearchStudent populateGradSearchStudent(Student student, String accessToken) {
		GradSearchStudent gradStu = new GradSearchStudent();
		BeanUtils.copyProperties(student, gradStu);
		GraduationStudentRecordEntity graduationStatusEntity = graduationStatusRepository.findByStudentID(UUID.fromString(student.getStudentID()));
		if(graduationStatusEntity != null) {
			GraduationStudentRecord gradObj = graduationStatusTransformer.transformToDTO(graduationStatusEntity);
			gradStu.setProgram(gradObj.getProgram());
			gradStu.setStudentGrade(gradObj.getStudentGrade());
			gradStu.setStudentStatus(gradObj.getStudentStatus());
			gradStu.setSchoolOfRecord(gradObj.getSchoolOfRecord());
		
			School school = webClient.get().uri(String.format(constants.getSchoolByMincodeUrl(), gradStu.getSchoolOfRecord())).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(School.class).block();
			if (school != null) {
				gradStu.setSchoolOfRecordName(school.getSchoolName());
				gradStu.setSchoolOfRecordindependentAffiliation(school.getIndependentAffiliation());
			}
		}
		return gradStu;
	}

    @Transactional
	@Retry(name = "searchbyid")
    public GradSearchStudent getStudentByStudentIDFromStudentAPI(String studentID, String accessToken) {
    	Student stuData = webClient.get().uri(String.format(constants.getPenStudentApiByStudentIdUrl(), studentID)).headers(h -> h.setBearerAuth(accessToken)).retrieve().bodyToMono(Student.class).block();
    	GradSearchStudent gradStu = new GradSearchStudent();
    	if (stuData != null) {
			gradStu = populateGradSearchStudent(stuData, accessToken);			
		}    	
    	return gradStu;
    }
}
