package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.messaging.GradStudentRecord;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordView;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GraduationStatusTransformer;
import ca.bc.gov.educ.api.gradstudent.repository.GraduationStudentRecordRepository;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.time.Month;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GradStudentService {
	private static final Logger logger = LoggerFactory.getLogger(GradStudentService.class);
	
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
	private static final String STD_NOT_FOUND_MSG = "Student with ID: %s not found";

	final EducGradStudentApiConstants constants;
	final WebClient studentApiClient;
	final GraduationStudentRecordRepository graduationStatusRepository;
	final GraduationStatusTransformer graduationStatusTransformer;

	@Autowired
	public GradStudentService(EducGradStudentApiConstants constants, @Qualifier("studentApiClient") WebClient studentApiClient, GraduationStudentRecordRepository graduationStatusRepository, GraduationStatusTransformer graduationStatusTransformer) {
		this.constants = constants;
		this.studentApiClient = studentApiClient;
		this.graduationStatusRepository = graduationStatusRepository;
		this.graduationStatusTransformer = graduationStatusTransformer;
	}

	public StudentSearch getStudentFromStudentAPI(StudentSearchRequest studentSearchRequest, Integer pageNumber, Integer pageSize, String accessToken) {
		List<GradSearchStudent> gradStudentList = new ArrayList<>();
		List<SearchCriteria> criteriaList = new ArrayList<>();
		populateSearchCriteria(studentSearchRequest, criteriaList);
		
		List<Search> searches = new LinkedList<>();
		StudentSearch searchObj = new StudentSearch();
	    searches.add(Search.builder().condition(Condition.AND).searchCriteriaList(criteriaList).build());
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
			String criteriaJSON = objectMapper.writeValueAsString(searches);
			String encodedURL = URLEncoder.encode(criteriaJSON,StandardCharsets.UTF_8.toString());
			RestResponsePage<Student> response = studentApiClient.get().uri(constants.getPenStudentApiSearchUrl(),
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
			logger.error(e.getMessage());
		}
		return null;
	}
	
	public GradOnlyStudentSearch getGRADStudents(StudentSearchRequest studentSearchRequest,Integer pageNumber,Integer pageSize,String accessToken) {
		
		Pageable paging = PageRequest.of(pageNumber, pageSize);
		List<GradSearchStudent> gradStudentList = new ArrayList<>();
		List<SearchCriteria> criteriaList = new ArrayList<>();
		ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
		Example<GraduationStudentRecordEntity> exampleQuery = Example.of(new GraduationStudentRecordEntity(studentSearchRequest.getGradProgram(),studentSearchRequest.getSchoolId()), matcher);
		Page<GraduationStudentRecordEntity> pagedResult = graduationStatusRepository.findAll(exampleQuery,paging);
		List<GraduationStudentRecordEntity> studList = pagedResult.getContent();
		if(!studList.isEmpty()) {
			String studentIds = studList.stream().map(std -> String.valueOf(std.getStudentID())).collect(Collectors.joining(","));
			getSearchCriteria(studentIds,null,"studentID",criteriaList);
		}
		populateSearchCriteria(studentSearchRequest, criteriaList);
		
		List<Search> searches = new LinkedList<>();
		GradOnlyStudentSearch searchObj = new GradOnlyStudentSearch();
	    searches.add(Search.builder().condition(Condition.AND).searchCriteriaList(criteriaList).build());
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
			String criteriaJSON = objectMapper.writeValueAsString(searches);
			String encodedURL = URLEncoder.encode(criteriaJSON,StandardCharsets.UTF_8.toString());
			RestResponsePage<Student> response = studentApiClient.get().uri(constants.getPenStudentApiSearchUrl(),
				uri -> uri
					.queryParam(PAGE_NUMBER, "0")
					.queryParam(PAGE_SIZE, studList.size())
					.queryParam(SEARCH_CRITERIA_LIST, encodedURL)
				.build())
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(new ParameterizedTypeReference<RestResponsePage<Student>>() {}).block();
			List<Student> studentList = response != null ? response.getContent() : new ArrayList<>();
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
	    	logger.error(e.getMessage());
		}
		
	    return null;
	}

	@Transactional
	@Retry(name = "advancedsearch")
	public GradOnlyStudentSearch getStudentFromStudentAPIGradOnly(StudentSearchRequest studentSearchRequest,String accessToken) {
		List<SearchCriteria> criteriaList = new ArrayList<>();
		populateSearchCriteria(studentSearchRequest, criteriaList);
		
		List<Search> searches = new LinkedList<>();
		GradOnlyStudentSearch searchObj = new GradOnlyStudentSearch();
	    searches.add(Search.builder().condition(Condition.AND).searchCriteriaList(criteriaList).build());
	    ObjectMapper objectMapper = new ObjectMapper();
	    try {
			String criteriaJSON = objectMapper.writeValueAsString(searches);
			String encodedURL = URLEncoder.encode(criteriaJSON,StandardCharsets.UTF_8.toString());
			RestResponsePage<Student> response = studentApiClient.get().uri(constants.getPenStudentApiSearchUrl(),
				uri -> uri
					.queryParam(PAGE_NUMBER, "0")
					.queryParam(PAGE_SIZE, "50000")
					.queryParam(SEARCH_CRITERIA_LIST, encodedURL)
				.build())
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(new ParameterizedTypeReference<RestResponsePage<Student>>() {}).block();
			List<Student> studentLists = response != null ? response.getContent():new ArrayList<>();
			if(studentLists.size() < 25000) {
				List<GradSearchStudent> gradStudentList = buildPartitions(studentLists, accessToken);
				searchObj.setGradSearchStudents(gradStudentList);
				searchObj.setSearchMessage(String.format(messageStringMoreMatchesFound, response != null ? (response.getTotalElements() - gradStudentList.size()) : 0));
			}else {
				searchObj.setGradSearchStudents(new ArrayList<>());
				searchObj.setSearchMessage("Change Search Criteria. Too many records as response");
			}
			return searchObj;
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

	private List<GradSearchStudent> buildPartitions(List<Student> studentLists, String accessToken) {
		List<GradSearchStudent> gradStudentList = new ArrayList<>();

		List<UUID> studentIds = studentLists.stream().map(std -> UUID.fromString(std.getStudentID())).collect(Collectors.toList());
		int partitionSize = 1000;
		List<List<UUID>> partitions = new LinkedList<>();
		for (int i = 0; i < studentIds.size(); i += partitionSize) {
			partitions.add(studentIds.subList(i, Math.min(i + partitionSize, studentIds.size())));
		}
		logger.debug(" partitions length {}", partitions.size());
		for (int i = 0; i < partitions.size(); i++) {
			List<UUID> subList = partitions.get(i);
			logger.debug(" sub list length {} par {}", subList.size(), i);
			List<GraduationStudentRecordView> gradList = graduationStatusRepository.findByStudentIDIn(subList);
			if (!gradList.isEmpty()) {
				gradList.forEach(st -> {
					GradSearchStudent gradStu = populateGradStudent(st, accessToken);
					if (gradStu.getProgram() != null) {
						gradStudentList.add(gradStu);
					}
				});
			}
		}
		return gradStudentList;
	}
	
	@Transactional
	@Retry(name = "searchbypen")
    public List<GradSearchStudent> getStudentByPenFromStudentAPI(String pen, String accessToken) {
    	List<GradSearchStudent> gradStudentList = new ArrayList<>();
    	List<Student> stuDataList = studentApiClient.get().uri(String.format(constants.getPenStudentApiByPenUrl(), pen))
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(new ParameterizedTypeReference<List<Student>>() {}).block();
    	if (stuDataList != null && !stuDataList.isEmpty()) {
			stuDataList.forEach(st -> {
				GradSearchStudent gradStu = populateGradSearchStudent(st, accessToken);
				gradStudentList.add(gradStu);
			});
		}
    	
    	return gradStudentList;
    }

    private void populateSearchCriteria(StudentSearchRequest studentSearchRequest, List<SearchCriteria> criteriaList) {
		getSearchCriteria(studentSearchRequest.getLegalFirstName(),null,LEGAL_FIRST_NAME,criteriaList);
		getSearchCriteria(studentSearchRequest.getLegalLastName(),null,LEGAL_LAST_NAME,criteriaList);
		getSearchCriteria(studentSearchRequest.getLegalMiddleNames(),null,LEGAL_MIDDLE_NAME,criteriaList);
		getSearchCriteria(studentSearchRequest.getUsualFirstName(),null,USUAL_FIRST_NAME,criteriaList);
		getSearchCriteria(studentSearchRequest.getUsualLastName(),null,USUAL_LAST_NAME,criteriaList);
		getSearchCriteria(studentSearchRequest.getUsualMiddleNames(),null,USUAL_MIDDLE_NAME,criteriaList);
		getSearchCriteria(studentSearchRequest.getLocalID(),null,LOCAL_ID,criteriaList);
		getSearchCriteria(studentSearchRequest.getGender(),null,GENDER_CODE,criteriaList);
		getSearchCriteria(studentSearchRequest.getBirthdateFrom(),studentSearchRequest.getBirthdateTo(),DOB,criteriaList);
		getSearchCriteria(studentSearchRequest.getMincode(),null,MINCODE,criteriaList);
	}

	private void getSearchCriteria(String value, String value2, String paramterType, List<SearchCriteria> criteriaList) {
		SearchCriteria criteria = null;
		if(paramterType.equalsIgnoreCase(DOB)) {
			if(StringUtils.isNotBlank(value) && StringUtils.isNotBlank(value2)) {
				criteria = SearchCriteria.builder().condition(Condition.AND).key(paramterType).operation(FilterOperation.BETWEEN).value(value + "," + value2).valueType(ValueType.DATE).build();
			}
		} else if (paramterType.equalsIgnoreCase("studentID")) {
			criteria = SearchCriteria.builder().key(paramterType).operation(FilterOperation.IN).value(value).valueType(ValueType.UUID).condition(Condition.AND).build();
		} else {
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
		return populateGradStudent(gradStu, accessToken);
	}

	private GradSearchStudent populateGradStudent(GraduationStudentRecordView gradRecord, String accessToken) {
		GradSearchStudent gradStu = new GradSearchStudent();
		BeanUtils.copyProperties(gradRecord, gradStu);
		gradStu.setStudentID(gradRecord.getStudentID().toString());
		gradStu.setSchoolOfRecordId(gradRecord.getSchoolOfRecordId() != null? gradRecord.getSchoolOfRecordId().toString() : null);
		return populateGradStudent(gradStu, accessToken);
	}

	private GradSearchStudent populateGradStudent(GradSearchStudent gradStu, String accessToken) {
		Student studentPen = studentApiClient.get().uri(String.format(constants.getPenStudentApiByStudentIdUrl(), gradStu.getStudentID()))
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(Student.class).block();
		if(studentPen != null) {
			BeanUtils.copyProperties(studentPen, gradStu);
		}
		SchoolClob schoolClob = studentApiClient.get().uri(String.format(constants.getSchoolClobBySchoolIdUrl(), gradStu.getSchoolOfRecordId()))
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(SchoolClob.class).block();
		if (schoolClob != null) {
			gradStu.setSchoolOfRecordName(schoolClob.getSchoolName());
		}
		return gradStu;
	}

    private GradSearchStudent populateGradSearchStudent(Student student, String accessToken) {
		GradSearchStudent gradStu = new GradSearchStudent();
		BeanUtils.copyProperties(student, gradStu);
		GraduationStudentRecordEntity graduationStatusEntity = graduationStatusRepository.findByStudentID(UUID.fromString(student.getStudentID()));
		if(graduationStatusEntity != null) {
			GraduationStudentRecord gradObj = graduationStatusTransformer.transformToDTOWithModifiedProgramCompletionDate(graduationStatusEntity);
			gradStu.setProgram(gradObj.getProgram());
			gradStu.setStudentGrade(gradObj.getStudentGrade());
			gradStu.setStudentStatus(gradObj.getStudentStatus());
			gradStu.setSchoolOfRecordId(gradObj.getSchoolOfRecordId() != null? gradObj.getSchoolOfRecordId().toString() : null);
			gradStu.setStudentCitizenship(gradObj.getStudentCitizenship());
		
			SchoolClob schoolClob = studentApiClient.get().uri(String.format(constants.getSchoolClobBySchoolIdUrl(), gradStu.getSchoolOfRecordId()))
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(SchoolClob.class).block();
			if (schoolClob != null) {
				gradStu.setSchoolOfRecord(schoolClob.getMinCode());
				gradStu.setTranscriptEligibility(schoolClob.getTranscriptEligibility());
				gradStu.setCertificateEligibility(schoolClob.getCertificateEligibility());
				gradStu.setSchoolOfRecordName(schoolClob.getSchoolName());
			}
		}
		return gradStu;
	}

    @Transactional
	@Retry(name = "searchbyid")
    public GradSearchStudent getStudentByStudentIDFromStudentAPI(String studentID, String accessToken) {
    	Student stuData = studentApiClient.get().uri(String.format(constants.getPenStudentApiByStudentIdUrl(), studentID))
				.headers(h -> h.setBearerAuth(accessToken))
				.retrieve().bodyToMono(Student.class).block();
    	GradSearchStudent gradStu = new GradSearchStudent();
    	if (stuData != null) {
			gradStu = populateGradSearchStudent(stuData, accessToken);			
		}    	
    	return gradStu;
    }

	@Transactional
	@Retry(name = "searchbyid")
	public GraduationStudentRecordDistribution getStudentByStudentIDFromGrad(String studentID,String accessToken) {
		return graduationStatusTransformer.tToDForDistribution(graduationStatusRepository.findByStudentID(UUID.fromString(studentID)), getStudentByStudentIDFromStudentAPI(studentID, accessToken));
	}

	@Transactional
	public Student addNewPenFromStudentAPI(StudentCreate student, String accessToken) {
		return studentApiClient.post()
				.uri(constants.getPenStudentApiUrl())
				.headers(h -> h.setBearerAuth(accessToken))
				.body(BodyInserters.fromValue(student))
				.retrieve().bodyToMono(Student.class).block();
	}

	@Transactional
	public List<UUID> getStudentIDsByStatusCode(List<UUID> studentIDs, String statusCode) {
		if (StringUtils.isBlank(statusCode) || studentIDs.isEmpty()) {
			return new ArrayList<>();
		}
		List<UUID> results = new ArrayList<>();
		int pageSize = 1000;
		int pageNum = studentIDs.size() / pageSize + 1;
		for (int i = 0; i < pageNum; i++) {
			int startIndex = i * pageSize;
			int endIndex = Math.min(startIndex + pageSize, studentIDs.size());
			List<UUID> inputIDs = studentIDs.subList(startIndex, endIndex);
			List<UUID> responseIDs = graduationStatusRepository.filterGivenStudentsByStatusCode(inputIDs, statusCode);
			if (responseIDs != null && !responseIDs.isEmpty()) {
				results.addAll(responseIDs);
			}
		}
		return results;
	}

	public List<UUID> getStudentIDsBySearchCriteriaOrAll(StudentSearchRequest searchRequest) {
		ArrayList<UUID> result = new ArrayList<>();
		if(searchRequest.getStudentIDs() != null && !searchRequest.getStudentIDs().isEmpty()) {
			result.addAll(searchRequest.getStudentIDs());
		}
		if(searchRequest.getPens() != null && !searchRequest.getPens().isEmpty()) {
			result.addAll(graduationStatusRepository.findStudentIDsByPenIn(searchRequest.getPens()));
		}
		if(searchRequest.getSchoolIds() != null && !searchRequest.getSchoolIds().isEmpty()) {
			result.addAll(graduationStatusRepository.findBySchoolOfRecordIdIn(searchRequest.getSchoolIds()));
		}
		return result;
	}

	public List<GraduationCountProjection> getGraduationCountsBySchools(List<UUID> schoolIDs) {
		if (schoolIDs == null || schoolIDs.isEmpty()) {
			return new ArrayList<>();
		}

		return graduationStatusRepository.countCurrentGraduatesAndNonGraduatesBySchoolOfRecordIn(schoolIDs);
	}

	/**
	 * Returns a condensed version of GraduationStudentRecord for GDC
	 * @param studentID
	 * @return
	 * @throws EntityNotFoundException
	 */
	public GradStudentRecord getGraduationStudentRecord(UUID studentID) {
		GradStudentRecord response = graduationStatusRepository.findByStudentID(studentID, GradStudentRecord.class);
		if (response != null) {
			logger.debug("getGraduationStudentRecord - studentID: {}, response: {}", studentID, response);
			return response;
		}
		throw new EntityNotFoundException(String.format(STD_NOT_FOUND_MSG, studentID));
	}

	public Boolean parseGraduationStatus(String studentProjectedGradData) {
		if (studentProjectedGradData == null || studentProjectedGradData.isEmpty()) {
			return false;
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			JsonNode jsonNode = mapper.readTree(studentProjectedGradData);
			return jsonNode.get("graduated").asBoolean();
		} catch (JsonProcessingException e) {
			return false;
		}
	}
}
