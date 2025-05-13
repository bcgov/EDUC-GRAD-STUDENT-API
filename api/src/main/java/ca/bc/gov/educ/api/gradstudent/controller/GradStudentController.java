package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.model.transformer.GradStudentPaginationTransformer;
import ca.bc.gov.educ.api.gradstudent.model.transformer.ReportGradStudentTransformer;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentPaginationService;
import ca.bc.gov.educ.api.gradstudent.service.ReportGradStudentSearchService;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.JsonUtil;
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

@RestController
@CrossOrigin
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Student Demographics.", description = "This API is for Reading demographics data of a student.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_DATA"})})
public class GradStudentController {

    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(GradStudentController.class);

    private final GradStudentService gradStudentService;
	private final ReportGradStudentSearchService reportGradStudentSearchService;
	private final GradStudentPaginationService gradStudentPaginationService;
	private final ReportGradStudentTransformer reportGradStudentTransformer;
	private final GradStudentPaginationTransformer gradStudentPaginationTransformer;

    public GradStudentController(GradStudentService gradStudentService, ReportGradStudentSearchService reportGradStudentSearchService, GradStudentPaginationService gradStudentPaginationService, ReportGradStudentTransformer reportGradStudentTransformer, GradStudentPaginationTransformer gradStudentPaginationTransformer) {
    	this.gradStudentService = gradStudentService;
        this.reportGradStudentSearchService = reportGradStudentSearchService;
        this.gradStudentPaginationService = gradStudentPaginationService;
        this.reportGradStudentTransformer = reportGradStudentTransformer;
        this.gradStudentPaginationTransformer = gradStudentPaginationTransformer;
    }
	
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
    public GradSearchStudent getGradStudentByStudentIDFromStudentAPI(@PathVariable String studentID, @RequestHeader(name="Authorization") String accessToken) {
    	return gradStudentService.getStudentByStudentIDFromStudentAPI(studentID,accessToken.replaceAll("Bearer ", ""));
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
						JsonUtil.mapper,
						sorts
				);
		return this.reportGradStudentSearchService
				.findAll(studentSpecs, pageNumber, pageSize, sorts)
				.thenApplyAsync(student -> student.map(reportGradStudentTransformer::transformToDTO));
	}

	@GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_PAGINATION)
	@PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
	@Transactional(readOnly = true)
	@ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
	public CompletableFuture<Page<GraduationStudentPaginationRecord>> findAllStudentPagination(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
																					@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
																					@RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
																					@RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson){
		final List<Sort.Order> sorts = new ArrayList<>();
		Specification<GraduationStudentRecordPaginationEntity> studentSpecs = gradStudentPaginationService
				.setSpecificationAndSortCriteria(
						sortCriteriaJson,
						searchCriteriaListJson,
						JsonUtil.mapper,
						sorts
				);
		return this.gradStudentPaginationService
				.findAll(studentSpecs, pageNumber, pageSize, sorts)
				.thenApplyAsync(student -> student.map(gradStudentPaginationTransformer::transformToDTO));
	}
}
