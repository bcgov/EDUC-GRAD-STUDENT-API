package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.service.DataConversionService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.PermissionsConstants;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Grad Student Status.", description = "This API is for Grad Student Status.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_DATA", "UPDATE_GRAD_GRADUATION_STATUS"})})
public class DataConversionController {

    private static final Logger logger = LoggerFactory.getLogger(DataConversionController.class);
    private static final String BEARER = "Bearer ";

    @Autowired
    DataConversionService dataConversionService;

    @Autowired
    GradValidation validation;

    @Autowired
    ResponseHelper response;

    @PostMapping(EducGradStudentApiConstants.CONV_GRADUATION_STATUS_BY_STUDENT_ID)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Save Graduation Student Record", description = "Save Graduation Student Record", tags = { "Data Conversion" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GraduationStudentRecord> saveStudentGradStatus(@PathVariable String studentID,
                                                                         @RequestParam(value = "ongoingUpdate", required = false, defaultValue = "false") boolean ongoingUpdate,
                                                                         @RequestBody GraduationStudentRecord graduationStatus) {
        logger.debug("Save Graduation Student Record for Student ID");
        var result = dataConversionService.saveGraduationStudentRecord(UUID.fromString(studentID),graduationStatus, ongoingUpdate);
        return response.GET(result);
    }

    @PostMapping(EducGradStudentApiConstants.CONV_GRADUATION_STATUS_FOR_ONGOING_UPDATES)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Update Graduation Status At Field Level for Ongoing Updates", description = "Update Graduation Status At Field Level for Ongoing Updates", tags = { "Data Conversion" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GraduationStudentRecord> updateGraduationStatusForOngoingUpdates(@RequestBody OngoingUpdateRequestDTO requestDTO,
                                                                                           @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Save Graduation Student Record for Student ID");
        var result = dataConversionService.updateGraduationStatusByFields(requestDTO, accessToken.replace(BEARER, ""));
        return response.GET(result);
    }

    @PostMapping (EducGradStudentApiConstants.CONV_STUDENT_OPTIONAL_PROGRAM)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Update/Create Student Optional Program", description = "Update/Create Student Optional Program", tags = { "Data Conversion" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<StudentOptionalProgram> saveStudentOptionalProgram(@RequestBody StudentOptionalProgramRequestDTO gradStudentOptionalProgramReq,
                                                                            @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Save Student Optional Program");
        return response.GET(dataConversionService.saveStudentOptionalProgram(gradStudentOptionalProgramReq,accessToken.replace(BEARER, "")));
    }

    @PostMapping (EducGradStudentApiConstants.CONV_STUDENT_CAREER_PROGRAM)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Update/Create Student Career Program", description = "Update/Create Student Career Program", tags = { "Data Conversion" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<StudentCareerProgram> saveStudentCareerProgram(@RequestBody StudentCareerProgram gradStudentCareerProgramReq) {
        logger.debug("Save Student Career Program ");
        return response.GET(dataConversionService.saveStudentCareerProgram(gradStudentCareerProgramReq));
    }

    @DeleteMapping (EducGradStudentApiConstants.CONV_STUDENT_OPTIONAL_PROGRAM_BY_STUDENT_ID)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Delete Student Optional Program", description = "Delete Student Optional Program", tags = { "Data Conversion" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Void> deleteStudentOptionalProgram(@PathVariable String optionalProgramID,
                                                                @PathVariable String studentID) {
        logger.debug("Delete Student Optional Program ");
        dataConversionService.deleteStudentOptionalProgram(UUID.fromString(optionalProgramID), UUID.fromString(studentID));
        return response.DELETE(1);
    }

    @DeleteMapping (EducGradStudentApiConstants.CONV_STUDENT_CAREER_PROGRAM_BY_STUDENT_ID)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Delete Student Career Program", description = "Delete Student Career Program", tags = { "Data Conversion" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Void> deleteStudentCareerProgram(@PathVariable String careerProgramCode,
                                                             @PathVariable String studentID) {
        logger.debug("Delete Student Career Program ");
        dataConversionService.deleteStudentCareerProgram(careerProgramCode, UUID.fromString(studentID));
        return response.DELETE(1);
    }

    @DeleteMapping(EducGradStudentApiConstants.CONV_GRADUATION_STATUS_BY_STUDENT_ID)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Delete All Student Related Data", description = "Delete All Student Related Data", tags = { "Data Conversion" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Void> deleteAll(@PathVariable String studentID) {
        logger.debug("Delete All Student Related Data for Student ID [{}]", studentID);
        dataConversionService.deleteAllDependencies(UUID.fromString(studentID));
        dataConversionService.deleteGraduationStatus(UUID.fromString(studentID));
        return response.DELETE(1);
    }

}
