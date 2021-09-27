package ca.bc.gov.educ.api.gradstudent.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.educ.api.gradstudent.dto.GradOnlyStudentSearch;
import ca.bc.gov.educ.api.gradstudent.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.dto.StudentSearch;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController
@EnableResourceServer
@CrossOrigin
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Student Demographics.", description = "This API is for Reading demographics data of a student.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_DATA"})})
public class GradStudentController {

    @SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(GradStudentController.class);

    private final GradStudentService gradStudentService;

    public GradStudentController(GradStudentService gradStudentService) {
    	this.gradStudentService = gradStudentService;
	}
	
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_ANY_NAME_ONLY)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_DATA')")
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
			@RequestParam(value = "birthdateTo", required = false) String birthdateTo) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getStudentFromStudentAPIGradOnly(legalFirstName,legalLastName,legalMiddleNames,usualFirstName,usualLastName,usualMiddleNames,gender,mincode,localID,birthdateFrom,birthdateTo,accessToken);
		
	}
    
    @GetMapping(EducGradStudentApiConstants.SEARCH_GRAD_STUDENTS)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "Search For Students", description = "Advanced Search for Student Demographics", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public GradOnlyStudentSearch getGRADStudents(
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
			@RequestParam(value = "schoolOfRecord", required = false) String schoolOfRecord,
			@RequestParam(value = "gradProgram", required = false) String gradProgram,
			@RequestParam(value = "pageNumber", required = false, defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", required = false, defaultValue = "20") Integer pageSize) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getGRADStudents(legalFirstName,legalLastName,legalMiddleNames,usualFirstName,usualLastName,usualMiddleNames,gender,mincode,localID,birthdateFrom,birthdateTo,schoolOfRecord,gradProgram,pageNumber,pageSize,accessToken);
		
	}
	
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_ANY_NAME)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_AND_PEN_STUDENT_DATA')")
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
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getStudentFromStudentAPI(legalFirstName,legalLastName,legalMiddleNames,usualFirstName,usualLastName,usualMiddleNames,gender,mincode,localID,birthdateFrom,birthdateTo,pageNumber,pageSize,accessToken);
		
	}
    
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_PEN_STUDENT_API)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "Search For Students by PEN", description = "Search for Student Demographics by PEN", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<GradSearchStudent> getGradStudentByPenFromStudentAPI(@PathVariable String pen) {
    	OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getStudentByPenFromStudentAPI(pen,accessToken);
    }
    
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_STUDENT_ID_STUDENT_API)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "GET Student by STUDENT ID", description = "Get Student Demographics by Student ID", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public GradSearchStudent getGradStudentByStudentIDFromStudentAPI(@PathVariable String studentID) {
    	OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getStudentByStudentIDFromStudentAPI(studentID,accessToken);
    }
}
