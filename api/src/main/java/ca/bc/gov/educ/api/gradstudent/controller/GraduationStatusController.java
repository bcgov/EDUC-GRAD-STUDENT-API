package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordHistoryEntity;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.service.HistoryService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.PermissionsConstants;
import ca.bc.gov.educ.api.gradstudent.util.ResponseHelper;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Grad Student Status.", description = "This API is for Grad Student Status.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"UPDATE_GRAD_GRADUATION_STATUS"})})
public class GraduationStatusController {

    private static final Logger logger = LoggerFactory.getLogger(GraduationStatusController.class);
    private static final String BEARER = "Bearer ";

    @Autowired
    GraduationStatusService gradStatusService;

    @Autowired
    HistoryService historyService;

    @Autowired
    Publisher publisher;

    @Autowired
    GradValidation validation;

    @Autowired
    ResponseHelper response;

    @GetMapping (EducGradStudentApiConstants.GRADUATION_STATUS_BY_STUDENT_ID)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Student Grad Status by Student ID", description = "Get Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<GraduationStudentRecord> getStudentGradStatus(@PathVariable String studentID, @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Get Student Grad Status for studentID");
        GraduationStudentRecord gradResponse = gradStatusService.getGraduationStatus(UUID.fromString(studentID),accessToken.replace(BEARER, ""));
        if(gradResponse != null) {
            return response.GET(gradResponse);
        }else {
            return response.NO_CONTENT();
        }
    }

    @GetMapping (EducGradStudentApiConstants.GRADUATION_STATUS_BY_STUDENT_ID_FOR_ALGORITHM)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
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
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Save Student Grad Status by Student ID", description = "Save Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GraduationStudentRecord> saveStudentGradStatus(@PathVariable String studentID,
                                                                         @RequestBody GraduationStudentRecord graduationStatus,
                                                                         @RequestParam(required = false) Long batchId,
                                                                         @RequestHeader(name="Authorization") String accessToken) throws JsonProcessingException {
        logger.debug("Save student Grad Status for Student ID");
        var result = gradStatusService.saveGraduationStatus(UUID.fromString(studentID),graduationStatus,batchId,accessToken.replace(BEARER, ""));
        publishToJetStream(result.getRight());
        return response.GET(result.getLeft());
    }

    @PostMapping (EducGradStudentApiConstants.GRAD_STUDENT_UPDATE_BY_STUDENT_ID)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Update Student Grad Status by Student ID", description = "Update Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<GraduationStudentRecord> updateStudentGradStatus(@PathVariable String studentID,
                                                                           @RequestBody GraduationStudentRecord graduationStatus,
                                                                           @RequestHeader(name="Authorization") String accessToken) throws JsonProcessingException {
        logger.debug("update student Grad Status for Student ID");
        validation.requiredField(graduationStatus.getStudentID(), "Student ID");
        if(validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        var result = gradStatusService.updateGraduationStatus(UUID.fromString(studentID),graduationStatus,accessToken.replace(BEARER, ""));
        publishToJetStream(result.getRight());
        return response.GET(result.getLeft());
    }

    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_OPTIONAL_PROGRAM_BY_PEN)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Find all Student Optional Grad Status by Student ID", description = "Get All Student Optional Grad Status by Student ID", tags = { "Optional Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<List<StudentOptionalProgram>> getStudentGradOptionalPrograms(@PathVariable String studentID,
                                                                                       @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Get Student Grad Status for Student ID");
        List<StudentOptionalProgram> gradResponse = gradStatusService.getStudentGradOptionalProgram(UUID.fromString(studentID),accessToken.replace(BEARER, ""));
        if(!gradResponse.isEmpty()) {
            return response.GET(gradResponse);
        }else {
            return response.NO_CONTENT();
        }
    }

    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_OPTIONAL_PROGRAM_BY_PEN_PROGRAM_OPTIONAL_PROGRAM)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Find all Student Optional Grad Status by Student ID,Optional PROGRAM ID", description = "Get All Student Optional Grad Status by Student ID,Optional PROGRAM ID", tags = { "Optional Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<StudentOptionalProgram> getStudentGradOptionalProgram(@PathVariable String studentID,@PathVariable String optionalProgramID,
                                                                                @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Get Student Grad Status for Student ID");
        StudentOptionalProgram gradResponse = gradStatusService.getStudentGradOptionalProgramByProgramCodeAndOptionalProgramCode(UUID.fromString(studentID),optionalProgramID,accessToken.replace(BEARER, ""));
        if(gradResponse != null) {
            return response.GET(gradResponse);
        }else {
            return response.NO_CONTENT();
        }
    }

    @PostMapping (EducGradStudentApiConstants.SAVE_GRAD_STUDENT_OPTIONAL_PROGRAM)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Save Student Optional Grad Status by Student ID", description = "Save Student Optional Grad Status by Student ID", tags = { "Optional Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<StudentOptionalProgram> saveStudentGradOptionalProgram(@RequestBody StudentOptionalProgram gradStudentOptionalProgram) {
        logger.debug("Save student Grad Status for PEN: ");
        return response.GET(gradStatusService.saveStudentGradOptionalProgram(gradStudentOptionalProgram));
    }

    @PostMapping (EducGradStudentApiConstants.UPDATE_GRAD_STUDENT_OPTIONAL_PROGRAM)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Update/Create Student Optional Grad Status by Student ID", description = "Update/Create Student Optional Grad Status by Student ID", tags = { "Optional Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<StudentOptionalProgram> updateStudentGradOptionalProgram(@RequestBody StudentOptionalProgramReq gradStudentOptionalProgramReq,
                                                                                   @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Update student Grad Status for PEN: ");
        return response.GET(gradStatusService.updateStudentGradOptionalProgram(gradStudentOptionalProgramReq,accessToken.replace(BEARER, "")));
    }

    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_RECALCULATE)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Students For Batch Algorithm", description = "Get Students For Batch Algorithm", tags = { "Batch Algorithm" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<BatchGraduationStudentRecord>> getStudentsForGraduation() {
        logger.debug("getStudentsForGraduation:");
        return response.GET(gradStatusService.getStudentsForGraduation());
    }

    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_PROJECTED_RUN)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Students For Batch Projected Algorithm", description = "Get Students For Batch Projected Algorithm", tags = { "Batch Algorithm" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<BatchGraduationStudentRecord>> getStudentsForProjectedGraduation() {
        logger.debug("getStudentsForProjectedGraduation:");
        return response.GET(gradStatusService.getStudentsForProjectedGraduation());
    }

    @GetMapping (EducGradStudentApiConstants.STUDENT_RECORD_STUDENT_ID_BATCH_RUN)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Students For Batch Projected Algorithm", description = "Get Students For Batch Projected Algorithm", tags = { "Batch Algorithm" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GraduationStudentRecord> getDataForBatch(@PathVariable String studentID,@RequestHeader(name="Authorization") String accessToken) {
        logger.debug("getDataForBatch:");
        return response.GET(gradStatusService.getDataForBatch(UUID.fromString(studentID),accessToken.replace(BEARER, "")));
    }



    @PostMapping (EducGradStudentApiConstants.GRAD_STUDENT_BY_LIST_CRITERIAS)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Students by multiply criterias", description = "Find Students by multiply criterias", tags = { "Search Student Records" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GraduationStudentRecordSearchResult> searchGraduationStudentRecords(@RequestBody StudentSearchRequest searchRequest,
                                                                                              @RequestHeader(name="Authorization") String accessToken) {
        return response.GET(gradStatusService.searchGraduationStudentRecords(searchRequest, accessToken.replace(BEARER, "")));
    }

    @GetMapping(EducGradStudentApiConstants.GET_STUDENT_STATUS_BY_STATUS_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Check if Student Status is valid", description = "Check if Student Status is valid", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Boolean> getStudentStatus(@PathVariable String statusCode) {
        logger.debug("getStudentStatus : ");
        return response.GET(gradStatusService.getStudentStatus(statusCode));
    }

    @PostMapping (EducGradStudentApiConstants.UNGRAD_STUDENT)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Ungrad Student Grad Status by STudent ID", description = "Update Student Grad Status by Student ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<GraduationStudentRecord> ungradStudent(@PathVariable String studentID,  @RequestParam(value = "ungradReasonCode", required = false) String ungradReasonCode,
                                                                 @RequestParam(value = "ungradReasonDesc", required = false) String ungradReasonDesc,
                                                                 @RequestHeader(name="Authorization") String accessToken) throws JsonProcessingException {
        logger.debug("update student Grad Status for Student ID");
        validation.requiredField(studentID, "Student ID");
        if(validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        var result = gradStatusService.undoCompletionStudent(UUID.fromString(studentID),ungradReasonCode,ungradReasonDesc,accessToken.replace(BEARER, ""));
        publishToJetStream(result.getRight());
        return response.GET(result.getLeft());
    }

    private void publishToJetStream(final GradStatusEvent event) {
        if (event != null) {
            publisher.dispatchChoreographyEvent(event);
        }
    }

    @GetMapping(EducGradStudentApiConstants.RETURN_TO_ORIGINAL_STATE)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Incase algorithm errors out, bring the original record back", description = "Check if Student Status is valid", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Boolean> returnToOriginalState(@PathVariable String studentID, @RequestParam(value = "isGraduated", required = false, defaultValue = "false") boolean isGraduated) {
        logger.debug("getStudentStatus : ");
        return response.GET(gradStatusService.restoreGradStudentRecord(UUID.fromString(studentID),isGraduated));
    }

    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_HISTORY)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Get all edit history for a Student", description = "Get all edit history for a Student", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<GraduationStudentRecordHistory>> getStudentHistory(@PathVariable String studentID) {
        logger.debug("getStudentEditHistory:");
        List<GraduationStudentRecordHistory> historyList =historyService.getStudentEditHistory(UUID.fromString(studentID));
        Collections.sort(historyList, Comparator.comparing(GraduationStudentRecordHistory::getCreateDate));
        return response.GET(historyList);
    }

    @GetMapping(EducGradStudentApiConstants.GRAD_STUDENT_DEMOG_BY_PEN)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Get Student Demographics by PEN", description = "Search for Student Demographics by PEN", tags = { "Student Demographics" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public StudentDemographic getStudentDemographics(@PathVariable String pen, @RequestHeader(name="Authorization") String accessToken) {
        return gradStatusService.getStudentDemographics(pen,accessToken.replace(BEARER, ""));
    }

    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_OPTIONAL_PROGRAM_HISTORY)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Get all edit history for a Student Optional Program", description = "Get all edit history for a Student", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<StudentOptionalProgramHistory>> getStudentOptionalProgramHistory(@PathVariable String studentID,
                                                                                                @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("getStudentOptionalProgramEditHistory:");
        List<StudentOptionalProgramHistory> histList = historyService.getStudentOptionalProgramEditHistory(UUID.fromString(studentID),accessToken.replace(BEARER, ""));
        Collections.sort(histList, Comparator.comparing(StudentOptionalProgramHistory::getCreateDate));
        return response.GET(histList);
    }

    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_HISTORY_BY_ID)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Get history for a ID", description = "Get a history for a historyID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GraduationStudentRecordHistory> getStudentHistoryByID(@PathVariable String historyID) {
        logger.debug("getStudentEditHistory:");
        GraduationStudentRecordHistory historyObj =historyService.getStudentHistoryByID(UUID.fromString(historyID));
        return response.GET(historyObj);
    }

    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_OPTIONAL_PROGRAM_HISTORY_BY_ID)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Get Student Optional Program History by ID", description = "Get Student Optional Program History by ID", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<StudentOptionalProgramHistory> getStudentOptionalProgramHistoryByID(@PathVariable String historyID,
                                                                                              @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("getStudentOptionalProgramEditHistory:");
        StudentOptionalProgramHistory histObj = historyService.getStudentOptionalProgramHistoryByID(UUID.fromString(historyID),accessToken.replace(BEARER, ""));
        return response.GET(histObj);
    }

    @PostMapping (EducGradStudentApiConstants.GRADUATION_RECORD_BY_STUDENT_ID_PROJECTED_RUN)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Save Student Grad Status by Student ID for projected run", description = "Save Student Grad Status by Student ID for projected run", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GraduationStudentRecord> saveStudentGradStatusProjectedRun(@PathVariable String studentID, @RequestParam(required = false) Long batchId, @RequestBody ProjectedRunClob projectedRunClob) {
        logger.debug("Save Projected student Grad Status for Student ID");
        GraduationStudentRecord gradRecord =  gradStatusService.saveStudentRecordProjectedTVRRun(UUID.fromString(studentID),batchId,projectedRunClob);
        return response.GET(gradRecord);
    }

    @GetMapping (EducGradStudentApiConstants.GRAD_STUDENT_HISTORY_BY_BATCH_ID)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Get history for a Batch ID", description = "Get a history for a BatchId", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Page<GraduationStudentRecordHistoryEntity>> getStudentHistoryByBatchID(
            @PathVariable Long batchId, @RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize, @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("getStudentHistoryByBatchID:");
        Page<GraduationStudentRecordHistoryEntity> historyList = historyService.getStudentHistoryByBatchID(batchId,pageNumber,pageSize,accessToken.replace(BEARER, ""));
        return response.GET(historyList);
    }

    @PostMapping (EducGradStudentApiConstants.GRAD_STUDENT_BY_MULTIPLE_STUDENTIDS)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Get history for a Batch ID", description = "Get a history for a BatchId", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<GraduationStudentRecord>> getStudentDetailsByStudentIDs(@RequestBody StudentList sList) {
        logger.debug("getStudentDetailsByStudentIDs:");
        List<GraduationStudentRecord> studentList = gradStatusService.getStudentDataByStudentIDs(sList.getStudentids());
        return response.GET(studentList);
    }


    @GetMapping (EducGradStudentApiConstants.GET_STUDENTS_FOR_YEARLY_DISTRIBUTION)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Find Students for yearly run", description = "find students for yearly run", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<List<UUID>> getStudentsForYearlyRun() {
        logger.debug("getStudentsForYearlyRun");
        List<UUID> studentIds = gradStatusService.getStudentsForYearlyDistribution();
        if(!studentIds.isEmpty()) {
            return response.GET(studentIds);
        }else {
            return response.NO_CONTENT();
        }
    }

    @PostMapping (EducGradStudentApiConstants.GRADUATION_RECORD_BY_STUDENT_ID_DISTRIBUTION_RUN)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Save Student Grad Status by Student ID for projected run", description = "Save Student Grad Status by Student ID for projected run", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<GraduationStudentRecord> saveStudentGradStatusDistributionRun(@PathVariable String studentID, @RequestParam(name = "batchId",required = false) Long batchId,@RequestParam(name = "activityCode",required = false) String activityCode) {
        logger.debug("Save Distribution student Grad Status for Student ID");
        GraduationStudentRecord gradRecord =  gradStatusService.saveStudentRecordDistributionRun(UUID.fromString(studentID),batchId,activityCode);
        return response.GET(gradRecord);
    }

    @GetMapping (EducGradStudentApiConstants.STUDENT_LIST_FOR_SCHOOL_REPORT)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Get Students For School Report by mincode", description = "Get Students For School Report by mincode", tags = { "Batch Algorithm" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<GraduationStudentRecord>> getStudentsForSchoolReport(@PathVariable String schoolOfRecord) {
        logger.debug("getStudentsForSchoolReport:");
        return response.GET(gradStatusService.getStudentsForSchoolReport(schoolOfRecord));
    }
}