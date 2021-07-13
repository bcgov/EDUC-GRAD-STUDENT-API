package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.dto.GradStudentSpecialProgram;
import ca.bc.gov.educ.api.gradstudent.dto.GradStudentSpecialProgramReq;
import ca.bc.gov.educ.api.gradstudent.dto.GraduationStatus;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStatusApiConstants;
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
@RequestMapping(EducGradStatusApiConstants.GRADUATION_STATUS_API_ROOT_MAPPING)
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

    @GetMapping (EducGradStatusApiConstants.GRADUATION_STATUS_BY_STUDENT_ID)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Student Grad Status by PEN", description = "Get Student Grad Status by PEN", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<GraduationStatus> getStudentGradStatus(@PathVariable String studentID) {
        logger.debug("Get Student Grad Status for studentID");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        GraduationStatus gradResponse = gradStatusService.getGraduationStatus(UUID.fromString(studentID),accessToken);
        if(gradResponse != null) {
    		return response.GET(gradResponse);
    	}else {
    		return response.NO_CONTENT();
    	}
    }
    
    @GetMapping (EducGradStatusApiConstants.GRADUATION_STATUS_BY_STUDENT_ID_FOR_ALGORITHM)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Student Grad Status by Student ID for algorithm", description = "Get Student Grad Status by Student ID for algorithm", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<GraduationStatus> getStudentGradStatusForAlgorithm(@PathVariable String studentID) {
        logger.debug("Get Student Grad Status for studentID");
        GraduationStatus gradResponse = gradStatusService.getGraduationStatusForAlgorithm(UUID.fromString(studentID));
        if(gradResponse != null) {
    		return response.GET(gradResponse);
    	}else {
    		return response.NO_CONTENT();
    	}
    }
    
    @PostMapping (EducGradStatusApiConstants.GRADUATION_STATUS_BY_STUDENT_ID)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Save Student Grad Status by Student ID", description = "Save Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GraduationStatus> saveStudentGradStatus(@PathVariable String studentID, @RequestBody GraduationStatus graduationStatus) {
        logger.debug("Save student Grad Status for Student ID");        
        return response.GET(gradStatusService.saveGraduationStatus(UUID.fromString(studentID),graduationStatus));
    } 
    
    @PostMapping (EducGradStatusApiConstants.GRAD_STUDENT_UPDATE_BY_STUDENT_ID)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Update Student Grad Status by Student ID", description = "Update Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<GraduationStatus> updateStudentGradStatus(@PathVariable String studentID, @RequestBody GraduationStatus graduationStatus) {
        logger.debug("update student Grad Status for Student ID");
        validation.requiredField(graduationStatus.getPen(), "Student ID");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        if(validation.hasErrors()) {
    		validation.stopOnErrors();
    		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    	}
        return response.GET(gradStatusService.updateGraduationStatus(UUID.fromString(studentID),graduationStatus,accessToken));
    }
    
    @GetMapping (EducGradStatusApiConstants.GRAD_STUDENT_SPECIAL_PROGRAM_BY_PEN)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT_SPECIAL_PROGRAM)
    @Operation(summary = "Find all Student Special Grad Status by Student ID", description = "Get All Student Special Grad Status by Student ID", tags = { "Special Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<List<GradStudentSpecialProgram>> getStudentGradSpecialPrograms(@PathVariable String studentID) {
        logger.debug("Get Student Grad Status for Student ID");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        List<GradStudentSpecialProgram> gradResponse = gradStatusService.getStudentGradSpecialProgram(UUID.fromString(studentID),accessToken);
        if(!gradResponse.isEmpty()) {
    		return response.GET(gradResponse);
    	}else {
    		return response.NO_CONTENT();
    	}
    }
    
    @GetMapping (EducGradStatusApiConstants.GRAD_STUDENT_SPECIAL_PROGRAM_BY_PEN_PROGRAM_SPECIAL_PROGRAM)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT_SPECIAL_PROGRAM)
    @Operation(summary = "Find all Student Special Grad Status by Student ID,SPECIAL PROGRAM ID", description = "Get All Student Special Grad Status by Student ID,SPECIAL PROGRAM ID", tags = { "Special Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<GradStudentSpecialProgram> getStudentGradSpecialProgram(@PathVariable String studentID,@PathVariable String specialProgramID) {
        logger.debug("Get Student Grad Status for Student ID");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        GradStudentSpecialProgram gradResponse = gradStatusService.getStudentGradSpecialProgramByProgramCodeAndSpecialProgramCode(UUID.fromString(studentID),specialProgramID,accessToken);
        if(gradResponse != null) {
    		return response.GET(gradResponse);
    	}else {
    		return response.NO_CONTENT();
    	}
    }
    
    @PostMapping (EducGradStatusApiConstants.SAVE_GRAD_STUDENT_SPECIAL_PROGRAM)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT_SPECIAL_PROGRAM)
    @Operation(summary = "Save Student Special Grad Status by Student ID", description = "Save Student Special Grad Status by Student ID", tags = { "Special Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GradStudentSpecialProgram> saveStudentGradSpecialProgram(@RequestBody GradStudentSpecialProgram gradStudentSpecialProgram) {
        logger.debug("Save student Grad Status for PEN: ");
        return response.GET(gradStatusService.saveStudentGradSpecialProgram(gradStudentSpecialProgram));
    }
    
    @PostMapping (EducGradStatusApiConstants.UPDATE_GRAD_STUDENT_SPECIAL_PROGRAM)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT_SPECIAL_PROGRAM)
    @Operation(summary = "Update/Create Student Special Grad Status by Student ID", description = "Update/Create Student Special Grad Status by Student ID", tags = { "Special Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GradStudentSpecialProgram> updateStudentGradSpecialProgram(@RequestBody GradStudentSpecialProgramReq gradStudentSpecialProgramReq) {
        logger.debug("Update student Grad Status for PEN: ");
        OAuth2AuthenticationDetails auth = (OAuth2AuthenticationDetails) SecurityContextHolder.getContext().getAuthentication().getDetails(); 
    	String accessToken = auth.getTokenValue();
        return response.GET(gradStatusService.updateStudentGradSpecialProgram(gradStudentSpecialProgramReq,accessToken));
    }
    
    @GetMapping (EducGradStatusApiConstants.GRAD_STUDENT_RECALCULATE)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Students For Batch Algorithm", description = "Get Students For Batch Algorithm", tags = { "Batch Algorithm" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<GraduationStatus>> getStudentsForGraduation() {
        logger.debug("getStudentsForGraduation:");
        return response.GET(gradStatusService.getStudentsForGraduation());
    }
    
    @GetMapping(EducGradStatusApiConstants.GET_STUDENT_STATUS_BY_STATUS_CODE_MAPPING)
    @PreAuthorize(PermissionsContants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Check if Student Status is valid", description = "Check if Student Status is valid", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Boolean> getStudentStatus(@PathVariable String statusCode) { 
    	logger.debug("getStudentStatus : ");
        return response.GET(gradStatusService.getStudentStatus(statusCode));
    }
    
    @PostMapping (EducGradStatusApiConstants.UNGRAD_STUDENT)
    @PreAuthorize(PermissionsContants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Ungrad Student Grad Status by STudent ID", description = "Update Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<GraduationStatus> ungradStudent(@PathVariable String studentID,  @RequestParam(value = "ungradReasonCode", required = false) String ungradReasonCode,
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
    
}