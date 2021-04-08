package ca.bc.gov.educ.api.gradstudent.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.educ.api.gradstudent.endpoint.GradStudentEndpoint;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentService;
import ca.bc.gov.educ.api.gradstudent.struct.GradSearchStudent;
import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;
import ca.bc.gov.educ.api.gradstudent.struct.StudentSearch;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;

@RestController
@EnableResourceServer
@Slf4j
@CrossOrigin
public class GradStudentController implements GradStudentEndpoint {

    private static Logger logger = LoggerFactory.getLogger(GradStudentController.class);

    @Autowired
    GradStudentService gradStudentService;

    @Override
    @Operation(summary = "Find Student by Pen", description = "Get a Student by Pen", tags = { "Student Demographics" }, deprecated = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public GradStudent getGradStudentByPen(String pen) {
    	OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getStudentByPen(pen,accessToken);
    }

	@Override
	@Operation(summary = "Find Student by Last Name", description = "Get a Student by LastName", tags = { "Student Demographics" }, deprecated = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public List<GradStudent> getGradStudentByLastName(String lastName,Integer pageNo, Integer pageSize) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
		return gradStudentService.getStudentByLastName(lastName,pageNo,pageSize,accessToken);
	}
	
	@Override
	@Operation(summary = "Find Student by First Name", description = "Get a Student by First Name", tags = { "Student Demographics" }, deprecated = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public List<GradStudent> getGradStudentByFirstName(String firstName, Integer pageNo, Integer pageSize) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
		return gradStudentService.getStudentByFirstName(firstName,pageNo,pageSize,accessToken);
	}
	
	@Override
	@Operation(summary = "Find Student by First Name and Last Name", description = "Get a Student by First Name and Last Name", tags = { "Student Demographics" }, deprecated = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public List<GradStudent> getGradStudentByLastNameAndFirstName(String lastName,String firstName, Integer pageNo, Integer pageSize) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
		return gradStudentService.getStudentByLastNameAndFirstName(lastName,firstName,pageNo,pageSize,accessToken);
	}
	
	@Override
	@Operation(summary = "Find Student Demographics for Multiple Pens", description = "Get Multiple Student Demographics by PENS", tags = { "Student Demographics" }, deprecated = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<GradStudent> getGradStudentByPens(@RequestParam(value = "penList", required = true) List<String> penList) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getStudentByPens(penList,accessToken);
    }
	
	@Override
	@Operation(summary = "Search For Students", description = "Advanced Search for Student Demographics", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
	public StudentSearch getGradStudentFromStudentAPI(
			String legalFirstName,
			String legalLastName,
			String legalMiddleNames,
			String usualFirstName,
			String usualLastName,
			String usualMiddleNames,
			String gender,
			String mincode,
			String localID,
			String birthdateFrom,
			String birthdateTo,
			Integer pageNumber,
			Integer pageSize) {
		OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getStudentFromStudentAPI(legalFirstName,legalLastName,legalMiddleNames,usualFirstName,usualLastName,usualMiddleNames,gender,mincode,localID,birthdateFrom,birthdateTo,pageNumber,pageSize,accessToken);
		
	}
	
	@Override
	@Operation(summary = "Search For Students by PEN", description = "Search for Student Demographics by PEN", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public List<GradSearchStudent> getGradStudentByPenFromStudentAPI(String pen) {
    	OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return gradStudentService.getStudentByPenFromStudentAPI(pen,accessToken);
    }
}
