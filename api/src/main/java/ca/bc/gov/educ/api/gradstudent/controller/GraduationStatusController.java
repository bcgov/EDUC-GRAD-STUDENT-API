package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.dto.StudentOptionalProgram;
import ca.bc.gov.educ.api.gradstudent.dto.StudentOptionalProgramReq;
import ca.bc.gov.educ.api.gradstudent.dto.GraduationStudentRecord;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
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

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@EnableResourceServer
@OpenAPIDefinition(info = @Info(title = "API for Grad Student Status.", description = "This API is for Grad Student Status.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"UPDATE_GRAD_GRADUATION_STATUS"})})
public class GraduationStatusController {

	private static Logger logger = LoggerFactory.getLogger(GraduationStatusController.class);

    @Autowired
    GraduationStatusService gradStatusService;
    
    @Autowired
	GradValidation validation;
    
    @Autowired
	ResponseHelper response;

    @GetMapping (EducGradStudentApiConstants.GRADUATION_STATUS_BY_STUDENT_ID)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Student Grad Status by Student ID", description = "Get Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<GraduationStudentRecord> getStudentGradStatus(@PathVariable String studentID) {
        logger.debug("Get Student Grad Status for studentID");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        GraduationStudentRecord gradResponse = gradStatusService.getGraduationStatus(UUID.fromString(studentID),accessToken);
        if(gradResponse != null) {
    		return response.GET(gradResponse);
    	}else {
    		return response.NO_CONTENT();
    	}
    }
    
    @GetMapping (EducGradStudentApiConstants.GRADUATION_STATUS_BY_STUDENT_ID_FOR_ALGORITHM)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Student Grad Status by Student ID for algorithm", description = "Get Student Grad Status by Student ID for algorithm", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<GraduationStudentRecord> getStudentGradStatusForAlgorithm(@PathVariable String studentID) {
        logger.debug("Get Student Grad Status for studentID");
        GraduationStudentRecord gradResponse = gradStatusService.getGraduationStatusForAlgorithm(UUID.fromString(studentID));
        if(gradResponse != null) {
    		return response.GET(gradResponse);
    	}else {
    		return response.NO_CONTENT();
    	}
    }
    
    @PostMapping (EducGradStudentApiConstants.GRADUATION_STATUS_BY_STUDENT_ID)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Save Student Grad Status by Student ID", description = "Save Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GraduationStudentRecord> saveStudentGradStatus(@PathVariable String studentID, @RequestBody GraduationStudentRecord graduationStatus) {
        logger.debug("Save student Grad Status for Student ID");        
        return response.GET(gradStatusService.saveGraduationStatus(UUID.fromString(studentID),graduationStatus));
    } 
    
    @PostMapping (EducGradStudentApiConstants.GRAD_STUDENT_UPDATE_BY_STUDENT_ID)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Update Student Grad Status by Student ID", description = "Update Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<GraduationStudentRecord> updateStudentGradStatus(@PathVariable String studentID, @RequestBody GraduationStudentRecord graduationStatus) {
        logger.debug("update student Grad Status for Student ID");
        validation.requiredField(graduationStatus.getStudentID(), "Student ID");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        if(validation.hasErrors()) {
    		validation.stopOnErrors();
    		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    	}
        return response.GET(gradStatusService.updateGraduationStatus(UUID.fromString(studentID),graduationStatus,accessToken));
    }
    
    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_SPECIAL_PROGRAM_BY_PEN)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT_SPECIAL_PROGRAM)
    @Operation(summary = "Find all Student Special Grad Status by Student ID", description = "Get All Student Special Grad Status by Student ID", tags = { "Special Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<List<StudentOptionalProgram>> getStudentGradSpecialPrograms(@PathVariable String studentID) {
        logger.debug("Get Student Grad Status for Student ID");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        List<StudentOptionalProgram> gradResponse = gradStatusService.getStudentGradSpecialProgram(UUID.fromString(studentID),accessToken);
        if(!gradResponse.isEmpty()) {
    		return response.GET(gradResponse);
    	}else {
    		return response.NO_CONTENT();
    	}
    }
    
    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_SPECIAL_PROGRAM_BY_PEN_PROGRAM_SPECIAL_PROGRAM)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT_SPECIAL_PROGRAM)
    @Operation(summary = "Find all Student Special Grad Status by Student ID,SPECIAL PROGRAM ID", description = "Get All Student Special Grad Status by Student ID,SPECIAL PROGRAM ID", tags = { "Special Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<StudentOptionalProgram> getStudentGradSpecialProgram(@PathVariable String studentID,@PathVariable String specialProgramID) {
        logger.debug("Get Student Grad Status for Student ID");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        StudentOptionalProgram gradResponse = gradStatusService.getStudentGradSpecialProgramByProgramCodeAndSpecialProgramCode(UUID.fromString(studentID),specialProgramID,accessToken);
        if(gradResponse != null) {
    		return response.GET(gradResponse);
    	}else {
    		return response.NO_CONTENT();
    	}
    }
    
    @PostMapping (EducGradStudentApiConstants.SAVE_GRAD_STUDENT_SPECIAL_PROGRAM)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT_SPECIAL_PROGRAM)
    @Operation(summary = "Save Student Special Grad Status by Student ID", description = "Save Student Special Grad Status by Student ID", tags = { "Special Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<StudentOptionalProgram> saveStudentGradSpecialProgram(@RequestBody StudentOptionalProgram gradStudentSpecialProgram) {
        logger.debug("Save student Grad Status for PEN: ");
        return response.GET(gradStatusService.saveStudentGradSpecialProgram(gradStudentSpecialProgram));
    }
    
    @PostMapping (EducGradStudentApiConstants.UPDATE_GRAD_STUDENT_SPECIAL_PROGRAM)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT_SPECIAL_PROGRAM)
    @Operation(summary = "Update/Create Student Special Grad Status by Student ID", description = "Update/Create Student Special Grad Status by Student ID", tags = { "Special Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<StudentOptionalProgram> updateStudentGradSpecialProgram(@RequestBody StudentOptionalProgramReq gradStudentSpecialProgramReq) {
        logger.debug("Update student Grad Status for PEN: ");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return response.GET(gradStatusService.updateStudentGradSpecialProgram(gradStudentSpecialProgramReq,accessToken));
    }
    
    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_RECALCULATE)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Students For Batch Algorithm", description = "Get Students For Batch Algorithm", tags = { "Batch Algorithm" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<GraduationStudentRecord>> getStudentsForGraduation() {
        logger.debug("getStudentsForGraduation:");
        return response.GET(gradStatusService.getStudentsForGraduation());
    }
    
    @GetMapping(EducGradStudentApiConstants.GET_STUDENT_STATUS_BY_STATUS_CODE_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Check if Student Status is valid", description = "Check if Student Status is valid", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Boolean> getStudentStatus(@PathVariable String statusCode) { 
    	logger.debug("getStudentStatus : ");
        return response.GET(gradStatusService.getStudentStatus(statusCode));
    }
    
    @PostMapping (EducGradStudentApiConstants.UNGRAD_STUDENT)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Ungrad Student Grad Status by STudent ID", description = "Update Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<GraduationStudentRecord> ungradStudent(@PathVariable String studentID,  @RequestParam(value = "ungradReasonCode", required = false) String ungradReasonCode,
    		 @RequestParam(value = "ungradReasonDesc", required = false) String ungradReasonDesc) {
        logger.debug("update student Grad Status for Student ID");
        validation.requiredField(studentID, "Student ID");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        if(validation.hasErrors()) {
    		validation.stopOnErrors();
    		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    	}
        return response.GET(gradStatusService.ungradStudent(UUID.fromString(studentID),ungradReasonCode,ungradReasonDesc,accessToken));
    }
    
    @GetMapping(EducGradStudentApiConstants.RETURN_TO_ORIGINAL_STATE)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Incase algorithm errors out, bring the original record back", description = "Check if Student Status is valid", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Boolean> returnToOriginalState(@PathVariable String studentID, @RequestParam(value = "isGraduated", required = false, defaultValue = "false") boolean isGraduated) { 
    	logger.debug("getStudentStatus : ");
        return response.GET(gradStatusService.restoreGradStudentRecord(UUID.fromString(studentID),isGraduated));
    }
    
}