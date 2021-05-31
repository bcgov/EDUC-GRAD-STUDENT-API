package ca.bc.gov.educ.api.gradstudent.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

import ca.bc.gov.educ.api.gradstudent.dto.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.dto.GradStudent;
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
	private static Logger logger = LoggerFactory.getLogger(GradStudentController.class);

    @Autowired
    GradStudentService gradStudentService;

    /**
     * Gets Student details by pen.
     *
     * @param pen the pen
     * @return the student details by pen
     */
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_PEN)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_DATA')")
    @Operation(summary = "Find Student by Pen", description = "Get a Student by Pen", tags = { "Student Demographics" }, deprecated = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public GradStudent getGradStudentByPen(@PathVariable String pen) {
    	OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getStudentByPen(pen,accessToken);
    }

    /**
     * Gets Student details by lastname.
     *
     * @param lastName the lastname of a student
     * @param pageNo the page number to return
     * @param pageSize the number of result items in a page
     * @return the student details by pen
     */
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_LAST_NAME)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "Find Student by Last Name", description = "Get a Student by LastName", tags = { "Student Demographics" }, deprecated = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public List<GradStudent> getGradStudentByLastName(@RequestParam(value = "lastName", required = true) String lastName,
    		@RequestParam(value = "pageNo", required = false,defaultValue = "0") Integer pageNo, 
            @RequestParam(value = "pageSize", required = false,defaultValue = "50") Integer pageSize) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
		return gradStudentService.getStudentByLastName(lastName,pageNo,pageSize,accessToken);
	}
	
    /**
     * Gets Student details by first name.
     *
     * @param lastName the first name of a student
     * @param pageNo the page number to return
     * @param pageSize the number of result items in a page
     * @return the student details by pen
     */
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_FIRST_NAME)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "Find Student by First Name", description = "Get a Student by First Name", tags = { "Student Demographics" }, deprecated = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public List<GradStudent> getGradStudentByFirstName(@RequestParam(value = "firstName", required = true) String firstName,
    		@RequestParam(value = "pageNo", required = false,defaultValue = "0") Integer pageNo, 
            @RequestParam(value = "pageSize", required = false,defaultValue = "50") Integer pageSize) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
		return gradStudentService.getStudentByFirstName(firstName,pageNo,pageSize,accessToken);
	}
	
    /**
     * Gets Student details by lastname.
     *
     * @param lastName the lastname of a student
     * @param pageNo the page number to return
     * @param pageSize the number of result items in a page
     * @return the student details by pen
     */
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_ANY_NAMES)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "Find Student by First Name and Last Name", description = "Get a Student by First Name and Last Name", tags = { "Student Demographics" }, deprecated = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public List<GradStudent> getGradStudentByLastNameAndFirstName(@RequestParam(value = "lastName", required = false,defaultValue = "*") String lastName,
    		@RequestParam(value = "firstName", required = false,defaultValue = "*") String firstName,
    		@RequestParam(value = "pageNo", required = false,defaultValue = "0") Integer pageNo, 
            @RequestParam(value = "pageSize", required = false,defaultValue = "20") Integer pageSize) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
		return gradStudentService.getStudentByLastNameAndFirstName(lastName,firstName,pageNo,pageSize,accessToken);
	}
	
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_MULTIPLE_PENS)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "Find Student Demographics for Multiple Pens", description = "Get Multiple Student Demographics by PENS", tags = { "Student Demographics" }, deprecated = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<GradStudent> getGradStudentByPens(@RequestParam(value = "penList", required = true) List<String> penList) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getStudentByPens(penList,accessToken);
    }
	
    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_BY_ANY_NAME)
    @PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT_DATA')")
	@Operation(summary = "Search For Students", description = "Advanced Search for Student Demographics", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public StudentSearch getGradStudentFromStudentAPI(
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
}
