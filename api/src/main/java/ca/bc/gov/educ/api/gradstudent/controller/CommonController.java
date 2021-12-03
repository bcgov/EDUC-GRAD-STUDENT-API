package ca.bc.gov.educ.api.gradstudent.controller;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ca.bc.gov.educ.api.gradstudent.service.CommonService;
import ca.bc.gov.educ.api.gradstudent.util.ApiResponseModel;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.PermissionsContants;
import ca.bc.gov.educ.api.gradstudent.util.ResponseHelper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@CrossOrigin
@RestController
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@EnableResourceServer
@OpenAPIDefinition(info = @Info(title = "API for Common endpoints.", description = "This API is for Reading Common endpoints.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_UNGRAD_REASONS_DATA","READ_GRAD_STUDENT_CAREER_DATA"})})
public class CommonController {

    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);
    
    private static final String STATUS_CODE="Status Code";

    @Autowired
    CommonService commonService;
    
    @Autowired
	GradValidation validation;
    
    @Autowired
	ResponseHelper response;
    
    @GetMapping(EducGradStudentApiConstants.GET_STUDENT_CAREER_PROGRAM_BY_CAREER_PROGRAM_CODE_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRAD_STUDENT_CAREER_DATA)
    @Operation(summary = "Check if Career Program is valid", description = "Check if Career Program is valid", tags = { "Career Programs" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Boolean> getStudentCareerProgram(@PathVariable String cpCode) { 
    	logger.debug("getStudentCareerProgram : ");
        return response.GET(commonService.getStudentCareerProgram(cpCode));
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_CAREER_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRAD_STUDENT_CAREER_DATA)
    @Operation(summary = "Find Student Career Program by Student ID", description = "Find Student Career Program by Student ID", tags = { "Career Programs" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<StudentCareerProgram>> getAllStudentCareerProgramsList(@PathVariable String studentID) { 
    	logger.debug("getAllStudentCareerProgramsList : ");
    	OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return response.GET(commonService.getAllGradStudentCareerProgramList(studentID,accessToken));
    }
    
    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_NOTES_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRAD_STUDENT_NOTES_DATA)
    @Operation(summary = "Find Student Notes by Pen", description = "Get Student Notes By Pen", tags = { "Student Notes" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<StudentNote>> getAllStudentNotes(@PathVariable String studentID) { 
    	logger.debug("getAllStudentNotes : ");
        return response.GET(commonService.getAllStudentNotes(UUID.fromString(studentID)));
    }
    
    @PostMapping (EducGradStudentApiConstants.STUDENT_NOTES_MAPPING)
    @PreAuthorize(PermissionsContants.CREATE_OR_UPDATE_GRAD_STUDENT_NOTES_DATA)
    @Operation(summary = "Create Student Notes", description = "Create Student Notes", tags = { "Student Notes" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<ApiResponseModel<StudentNote>> saveStudentNotes(@RequestBody StudentNote studentNote) {
        logger.debug("Create student Grad Note for PEN: " + studentNote.getStudentID());
        validation.requiredField(studentNote.getStudentID(), "Pen");
        return response.UPDATED(commonService.saveStudentNote(studentNote));
    }
    
    @DeleteMapping(EducGradStudentApiConstants.STUDENT_NOTES_DELETE_MAPPING)
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
    
    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_STATUS_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find all Student Status", description = "Get all Student Status", tags = {"Student Status"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<StudentStatus>> getAllStudentStatusCodeList() {
        logger.debug("getAllUngradReasonCodeList : ");
        return response.GET(commonService.getAllStudentStatusCodeList());
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_STATUS_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find a Student Status by Student Status Code",
            description = "Get a Student Status by Student Status Code", tags = {"Student Status"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<StudentStatus> getSpecificStudentStatusCode(@PathVariable String statusCode) {
        logger.debug("getSpecificUngradReasonCode : ");
        StudentStatus gradResponse = commonService.getSpecificStudentStatusCode(statusCode);
        if (gradResponse != null) {
            return response.GET(gradResponse);
        } else {
            return response.NO_CONTENT();
        }

    }

    @PostMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_STATUS_MAPPING)
    @PreAuthorize(PermissionsContants.CREATE_STUDENT_STATUS)
    @Operation(summary = "Create a Student Status", description = "Create a Student Status", tags = {"Student Status"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<ApiResponseModel<StudentStatus>> createStudentStatus(@Valid @RequestBody StudentStatus studentStatus) {
        logger.debug("createStudentStatus : ");
        validation.requiredField(studentStatus.getCode(), STATUS_CODE);
        validation.requiredField(studentStatus.getDescription(), "Status Description");
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return response.CREATED(commonService.createStudentStatus(studentStatus));
    }

    @PutMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_STATUS_MAPPING)
    @PreAuthorize(PermissionsContants.UPDATE_STUDENT_STATUS)
    @Operation(summary = "Update an Student Status", description = "Update an Student Status", tags = {"Student Status"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<ApiResponseModel<StudentStatus>> updateStudentStatusCode(@Valid @RequestBody StudentStatus studentStatus) {
        logger.debug("updateStudentStatusCode : ");
        validation.requiredField(studentStatus.getCode(), STATUS_CODE);
        validation.requiredField(studentStatus.getDescription(), "Status Description");
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return response.UPDATED(commonService.updateStudentStatus(studentStatus));
    }

    @DeleteMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_STATUS_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsContants.DELETE_STUDENT_STATUS)
    @Operation(summary = "Delete an Student Status", description = "Delete an Student Status", tags = {"Student Status"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<Void> deleteStudentStatusCodes(@Valid @PathVariable String statusCode) {
        logger.debug("deleteStudentStatusCodes : ");
        validation.requiredField(statusCode, STATUS_CODE);
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return response.DELETE(commonService.deleteStudentStatus(statusCode));
    }
    
    @GetMapping (EducGradStudentApiConstants.STUDENT_ALGORITHM_DATA)
    @PreAuthorize(PermissionsContants.STUDENT_ALGORITHM_DATA)
    @Operation(summary = "Find Student Grad Status by Student ID for algorithm", description = "Get Student Grad Status by Student ID for algorithm", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<GradStudentAlgorithmData> getStudentGradStatusForAlgorithm(@PathVariable String studentID) {
        logger.debug("Get Student Grad Status for studentID");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        GradStudentAlgorithmData gradResponse = commonService.getGradStudentAlgorithmData(studentID,accessToken);
        if(gradResponse != null) {
    		return response.GET(gradResponse);
    	}else {
    		return response.NO_CONTENT();
    	}
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_HISTORY_ACTIVITY_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find all Student Status", description = "Get all Student Status", tags = {"Student Status"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<HistoryActivity>> getAllHistoryActivityCodeList() {
        logger.debug("getAllHistoryActivityCodeList : ");
        return response.GET(commonService.getAllHistoryActivityCodeList());
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_HISTORY_ACTIVITY_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find a History Activity Code by Code",
            description = "Find a History Activity Code by Code", tags = {"History Activity"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<HistoryActivity> getSpecificHistoryActivityCode(@PathVariable String activityCode) {
        logger.debug("getSpecificUngradReasonCode : ");
        HistoryActivity gradResponse = commonService.getSpecificHistoryActivityCode(activityCode);
        if (gradResponse != null) {
            return response.GET(gradResponse);
        } else {
            return response.NO_CONTENT();
        }

    }
   
}
