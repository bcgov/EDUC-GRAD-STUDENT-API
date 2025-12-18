package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStudentSearchDataEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCoursePaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.mapper.GradStudentSearchMapper;
import ca.bc.gov.educ.api.gradstudent.model.transformer.ReportGradStudentTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.StudentCoursePaginationTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.StudentOptionalProgramPaginationTransformer;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.service.ReportGradStudentSearchService;
import ca.bc.gov.educ.api.gradstudent.service.StudentCoursePaginationService;
import ca.bc.gov.educ.api.gradstudent.service.StudentOptionalProgramPaginationService;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentSearchService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.PermissionsConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static ca.bc.gov.educ.api.gradstudent.util.JsonUtil.mapper;

@RestController
@CrossOrigin
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Student Demographics.", description = "This API is for Reading demographics data of a student.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_DATA"})})
public class GradStudentController {

    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(GradStudentController.class);

    private final GradStudentService gradStudentService;
	private final ReportGradStudentSearchService reportGradStudentSearchService;
	private final StudentCoursePaginationService studentCoursePaginationService;
	private final StudentOptionalProgramPaginationService studentOptionalProgramPaginationService;
	private final ReportGradStudentTransformer reportGradStudentTransformer;
	private final StudentCoursePaginationTransformer gradStudentPaginationTransformer;
	private final StudentOptionalProgramPaginationTransformer studentOptionalProgramPaginationTransformer;
    private final GradStudentSearchService gradStudentSearchService;
    private static final GradStudentSearchMapper GRAD_STUDENT_SEARCH_MAPPER = GradStudentSearchMapper.mapper;

    public GradStudentController(GradStudentService gradStudentService, ReportGradStudentSearchService reportGradStudentSearchService, StudentCoursePaginationService studentCoursePaginationService, StudentOptionalProgramPaginationService studentOptionalProgramPaginationService, ReportGradStudentTransformer reportGradStudentTransformer, StudentCoursePaginationTransformer gradStudentPaginationTransformer, StudentOptionalProgramPaginationTransformer studentOptionalProgramPaginationTransformer, GradStudentSearchService gradStudentSearchService) {
    	this.gradStudentService = gradStudentService;
        this.reportGradStudentSearchService = reportGradStudentSearchService;
        this.studentCoursePaginationService = studentCoursePaginationService;
        this.studentOptionalProgramPaginationService = studentOptionalProgramPaginationService;
        this.reportGradStudentTransformer = reportGradStudentTransformer;
        this.gradStudentPaginationTransformer = gradStudentPaginationTransformer;
        this.studentOptionalProgramPaginationTransformer = studentOptionalProgramPaginationTransformer;
        this.gradStudentSearchService = gradStudentSearchService;
    }

    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_SEARCH_PAGINATION)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Search For Students using a search criteria list", description = "Paginated search for students")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
    public CompletableFuture<Page<GradStudentSearchData>> findStudents(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                       @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
                                                                       @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson){
        final List<Sort.Order> sorts = new ArrayList<>();
        Specification<GradStudentSearchDataEntity> studentSpecs = gradStudentSearchService
                .setSpecificationAndSortCriteria(
                        sortCriteriaJson,
                        searchCriteriaListJson,
                        mapper,
                        sorts
                );
        return this.gradStudentSearchService
                .findAll(studentSpecs, pageNumber, pageSize, sorts)
                .thenApplyAsync(studentSearchDataEntities -> studentSearchDataEntities.map(GRAD_STUDENT_SEARCH_MAPPER::toStructure));
    }

    /**
     * Deprecation notice. This endpoint will be discontinued in GRAD v1.24.0 Use
     * api/v1/student/search/pagination instead
     */
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_ANY_NAME_ONLY)
    @PreAuthorize("hasAuthority('SCOPE_READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "Search For Students", description = "Advanced Search for Student Demographics", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public GradOnlyStudentSearch getGradStudentsFromStudentAPI(
			@RequestParam(value = "legalFirstName", required = false) String legalFirstName,
			@RequestParam(value = "legalLastName", required = false) String legalLastName,
			@RequestParam(value = "legalMiddleNames", required = false) String legalMiddleNames,
			@RequestParam(value = "usualFirstName", required = false) String usualFirstName,
			@RequestParam(value = "usualLastName", required = false) String usualLastName,
			@RequestParam(value = "usualMiddleNames", required = false) String usualMiddleNames,
			@RequestParam(value = "gender", required = false) String gender,
			@RequestParam(value = "mincode", required = false) String mincode,
			@RequestParam(value = "localID", required = false) String localID,
			@RequestParam(value = "birthdateFrom", required = false) String birthdateFrom,
			@RequestParam(value = "birthdateTo", required = false) String birthdateTo,
			@RequestHeader(name="Authorization") String accessToken) {
		StudentSearchRequest studentSearchRequest = StudentSearchRequest.builder()
			.legalFirstName(legalFirstName).legalLastName(legalLastName).legalMiddleNames(legalMiddleNames)
			.usualFirstName(usualFirstName).usualLastName(usualLastName).usualMiddleNames(usualMiddleNames)
			.gender(gender).mincode(mincode).localID(localID).birthdateFrom(birthdateFrom).birthdateTo(birthdateTo)
			.build();
        return gradStudentService.getStudentFromStudentAPIGradOnly(studentSearchRequest,accessToken.replaceAll("Bearer ", ""));
		
	}
	
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_ANY_NAME)
    @PreAuthorize("hasAuthority('SCOPE_READ_GRAD_AND_PEN_STUDENT_DATA')")
	@Operation(summary = "Search For Students", description = "Advanced Search for Student Demographics", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public StudentSearch getGradNPenGradStudentFromStudentAPI(
			@RequestParam(value = "legalFirstName", required = false) String legalFirstName,
			@RequestParam(value = "legalLastName", required = false) String legalLastName,
			@RequestParam(value = "legalMiddleNames", required = false) String legalMiddleNames,
			@RequestParam(value = "usualFirstName", required = false) String usualFirstName,
			@RequestParam(value = "usualLastName", required = false) String usualLastName,
			@RequestParam(value = "usualMiddleNames", required = false) String usualMiddleNames,
			@RequestParam(value = "gender", required = false) String gender,
			@RequestParam(value = "mincode", required = false) String mincode,
			@RequestParam(value = "localID", required = false) String localID,
			@RequestParam(value = "birthdateFrom", required = false) String birthdateFrom,
			@RequestParam(value = "birthdateTo", required = false) String birthdateTo,
			@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			@RequestHeader(name="Authorization") String accessToken) {
		StudentSearchRequest studentSearchRequest = StudentSearchRequest.builder()
			.legalFirstName(legalFirstName).legalLastName(legalLastName).legalMiddleNames(legalMiddleNames)
			.usualFirstName(usualFirstName).usualLastName(usualLastName).usualMiddleNames(usualMiddleNames)
			.gender(gender).mincode(mincode).localID(localID).birthdateFrom(birthdateFrom).birthdateTo(birthdateTo)
			.build();
        return gradStudentService.getStudentFromStudentAPI(studentSearchRequest,pageNumber,pageSize,accessToken);
		
	}
    
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_PEN_STUDENT_API)
    @PreAuthorize("hasAuthority('SCOPE_READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "Search For Students by PEN", description = "Search for Student Demographics by PEN", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<GradSearchStudent> getGradStudentByPenFromStudentAPI(@PathVariable String pen, @RequestHeader(name="Authorization") String accessToken) {
    	 return gradStudentService.getStudentByPenFromStudentAPI(pen,accessToken.replaceAll("Bearer ", ""));
    }
    
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_STUDENT_ID_STUDENT_API)
    @PreAuthorize("hasAuthority('SCOPE_READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "GET Student by STUDENT ID", description = "Get Student Demographics by Student ID", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public GradSearchStudent getGradStudentByStudentIDFromStudentAPI(@PathVariable String studentID) {
    	return gradStudentService.getStudentByStudentIDFromStudentAPI(studentID);
    }

	@GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_STUDENT_ID_GRAD)
	@PreAuthorize("hasAuthority('SCOPE_READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "GET Student by STUDENT ID", description = "Get Student Demographics by Student ID", tags = { "Student Demographics" })
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public GraduationStudentRecordDistribution getGradStudentByStudentIDFromGRAD(@PathVariable String studentID, @RequestHeader(name="Authorization") String accessToken) {
		return gradStudentService.getStudentByStudentIDFromGrad(studentID, accessToken.replaceAll("Bearer ", ""));
	}

    @PostMapping
	@PreAuthorize("hasAuthority('SCOPE_WRITE_STUDENT')")
    public Student addNewPenFromStudentAPI(@Validated @RequestBody StudentCreate student, @RequestHeader(name="Authorization") String accessToken) {
		return gradStudentService.addNewPenFromStudentAPI(student, accessToken.replaceAll("Bearer ", ""));
	}

	@PostMapping (EducGradStudentApiConstants.GRAD_STUDENT_BY_SEARCH_CRITERIAS)
	@PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
	@Operation(summary = "Find Students by StudentSearchRequest criteria", description = "Find Students by StudentSearchRequest criteria", tags = { "Search Student Records" })
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public List<UUID> searchGraduationStudentRecords(@RequestBody StudentSearchRequest searchRequest) {
		return gradStudentService.getStudentIDsBySearchCriteriaOrAll(searchRequest);
	}

	@PostMapping(EducGradStudentApiConstants.GRADUATION_COUNTS)
	@PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
	@Operation(summary = "Get Graduation Counts by School IDs", description = "Retrieves counts of current graduates and non-graduates for specified schools.", tags = { "Student Demographics" })
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "OK"),
			@ApiResponse(responseCode = "401", description = "Unauthorized"),
			@ApiResponse(responseCode = "403", description = "Forbidden")
	})
	public List<GraduationCountProjection> getGraduationCountsBySchools(@RequestBody GraduationCountRequest requestBody) {
		List<UUID> schoolIDs = requestBody.getSchoolID();
		return gradStudentService.getGraduationCountsBySchools(schoolIDs);
	}
	
	@GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_REPORT_PAGINATION)
	@PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
	@Transactional(readOnly = true)
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
	public CompletableFuture<Page<ReportGradStudentData>> findAll(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
													 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
													 @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
													 @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson){
		final List<Sort.Order> sorts = new ArrayList<>();
		Specification<ReportGradStudentDataEntity> studentSpecs = reportGradStudentSearchService
				.setSpecificationAndSortCriteria(
						sortCriteriaJson,
						searchCriteriaListJson,
						mapper,
						sorts
				);
		return this.reportGradStudentSearchService
				.findAll(studentSpecs, pageNumber, pageSize, sorts)
				.thenApplyAsync(student -> student.map(reportGradStudentTransformer::transformToDTO));
	}

	@GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_COURSE_PAGINATION)
	@PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
	@Transactional(readOnly = true)
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
	public CompletableFuture<Page<StudentCoursePagination>> findAllStudentPagination(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
																						   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
																						   @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
																						   @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson){
		final List<Sort.Order> sorts = new ArrayList<>();
		Specification<StudentCoursePaginationEntity> studentSpecs = studentCoursePaginationService
				.setSpecificationAndSortCriteria(
						sortCriteriaJson,
						searchCriteriaListJson,
						mapper,
						sorts
				);
		return this.studentCoursePaginationService
				.findAll(studentSpecs, pageNumber, pageSize, sorts)
				.thenApplyAsync(student -> student.map(gradStudentPaginationTransformer::transformToDTO));
	}

	@GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_OPTIONAL_PROGRAM_PAGINATION)
	@PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
	@Transactional(readOnly = true)
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
	public CompletableFuture<Page<StudentOptionalProgramPagination>> findAllStudentOptionalProgramPagination(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
																						   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
																						   @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
																						   @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson){
		final List<Sort.Order> sorts = new ArrayList<>();
		Specification<StudentOptionalProgramPaginationEntity> studentSpecs = studentOptionalProgramPaginationService
				.setSpecificationAndSortCriteria(
						sortCriteriaJson,
						searchCriteriaListJson,
						mapper,
						sorts
				);
		return this.studentOptionalProgramPaginationService
				.findAll(studentSpecs, pageNumber, pageSize, sorts)
				.thenApplyAsync(student -> student.map(studentOptionalProgramPaginationTransformer::transformToDTO));
	}
}
