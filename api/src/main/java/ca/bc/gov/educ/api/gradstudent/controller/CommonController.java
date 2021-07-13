package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.dto.GradStudentCareerProgram;
import ca.bc.gov.educ.api.gradstudent.dto.StudentNote;
import ca.bc.gov.educ.api.gradstudent.service.CommonService;
import ca.bc.gov.educ.api.gradstudent.util.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping(EducGradCommonApiConstants.GRAD_COMMON_API_ROOT_MAPPING)
@EnableResourceServer
@OpenAPIDefinition(info = @Info(title = "API for Common endpoints.", description = "This API is for Reading Common endpoints.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_UNGRAD_REASONS_DATA","READ_GRAD_STUDENT_CAREER_DATA"})})
public class CommonController {

    private static Logger logger = LoggerFactory.getLogger(CommonController.class);

    @Autowired
    CommonService commonService;
    
    @Autowired
	GradValidation validation;
    
    @Autowired
	ResponseHelper response;
    
    @GetMapping(EducGradCommonApiConstants.GET_STUDENT_CAREER_PROGRAM_BY_CAREER_PROGRAM_CODE_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRAD_STUDENT_CAREER_DATA)
    @Operation(summary = "Check if Career Program is valid", description = "Check if Career Program is valid", tags = { "Career Programs" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Boolean> getStudentCareerProgram(@PathVariable String cpCode) { 
    	logger.debug("getStudentCareerProgram : ");
        return response.GET(commonService.getStudentCareerProgram(cpCode));
    }

    @GetMapping(EducGradCommonApiConstants.GET_ALL_STUDENT_CAREER_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRAD_STUDENT_CAREER_DATA)
    @Operation(summary = "Find Student Career Program by Pen", description = "Find Student Career Program by Pen", tags = { "Career Programs" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<GradStudentCareerProgram>> getAllStudentCareerProgramsList(@PathVariable String pen) { 
    	logger.debug("getAllStudentCareerProgramsList : ");
    	OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return response.GET(commonService.getAllGradStudentCareerProgramList(pen,accessToken));
    }
    
    @GetMapping(EducGradCommonApiConstants.GET_ALL_STUDENT_NOTES_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRAD_STUDENT_NOTES_DATA)
    @Operation(summary = "Find Student Notes by Pen", description = "Get Student Notes By Pen", tags = { "Student Notes" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<StudentNote>> getAllStudentNotes(@PathVariable String pen) { 
    	logger.debug("getAllStudentNotes : ");
        return response.GET(commonService.getAllStudentNotes(UUID.fromString(pen)));
    }
    
    @PostMapping (EducGradCommonApiConstants.STUDENT_NOTES_MAPPING)
    @PreAuthorize(PermissionsContants.CREATE_OR_UPDATE_GRAD_STUDENT_NOTES_DATA)
    @Operation(summary = "Create Student Notes", description = "Create Student Notes", tags = { "Student Notes" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<ApiResponseModel<StudentNote>> saveStudentNotes(@RequestBody StudentNote studentNote) {
        logger.debug("Create student Grad Note for PEN: " + studentNote.getStudentID());
        validation.requiredField(studentNote.getStudentID(), "Pen");
        return response.UPDATED(commonService.saveStudentNote(studentNote));
    }
    
    @DeleteMapping(EducGradCommonApiConstants.STUDENT_NOTES_DELETE_MAPPING)
    @PreAuthorize(PermissionsContants.DELETE_GRAD_STUDENT_NOTES_DATA)
    @Operation(summary = "Delete a note", description = "Delete a note", tags = { "Student Notes" })
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND.")})
    public ResponseEntity<Void> deleteNotes(@Valid @PathVariable String noteID) { 
    	logger.debug("deleteNotes : ");
    	validation.requiredField(noteID, "Note ID");
    	if(validation.hasErrors()) {
    		validation.stopOnErrors();
    		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    	}
        return response.DELETE(commonService.deleteNote(UUID.fromString(noteID)));
    }
   
}
