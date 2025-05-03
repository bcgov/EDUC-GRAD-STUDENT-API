package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.service.CommonService;
import ca.bc.gov.educ.api.gradstudent.service.GradStudentReportService;
import ca.bc.gov.educ.api.gradstudent.service.GraduationStatusService;
import ca.bc.gov.educ.api.gradstudent.util.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Common endpoints.", description = "This API is for Reading Common endpoints.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_UNGRAD_REASONS_DATA","READ_GRAD_STUDENT_CAREER_DATA"})})
@AllArgsConstructor
public class CommonController {

    private static final Logger logger = LoggerFactory.getLogger(CommonController.class);
    
    private static final String STATUS_CODE = "Status Code";
    private static final String BEARER = "Bearer ";

    CommonService commonService;

    GradStudentReportService gradStudentReportService;

    GraduationStatusService graduationStatusService;
    
	GradValidation validation;
    
	ResponseHelper response;
    
    @GetMapping(EducGradStudentApiConstants.GET_STUDENT_CAREER_PROGRAM_BY_CAREER_PROGRAM_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_CAREER_DATA)
    @Operation(summary = "Check if Career Program is valid", description = "Check if Career Program is valid", tags = { "Career Programs" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<Boolean> getStudentCareerProgram(@PathVariable String cpCode) { 
    	logger.debug("getStudentCareerProgram : ");
        return response.GET(commonService.getStudentCareerProgram(cpCode));
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_CAREER_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_CAREER_DATA)
    @Operation(summary = "Find Student Career Program by Student ID", description = "Find Student Career Program by Student ID", tags = { "Career Programs" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<StudentCareerProgram>> getAllStudentCareerProgramsList(@PathVariable String studentID,
                                                                                      @RequestHeader(name="Authorization") String accessToken) {
    	logger.debug("getAllStudentCareerProgramsList : ");
        return response.GET(commonService.getAllGradStudentCareerProgramList(studentID,accessToken.replace(BEARER, "")));
    }
    
    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_NOTES_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_NOTES_DATA)
    @Operation(summary = "Find Student Notes by Pen", description = "Get Student Notes By Pen", tags = { "Student Notes" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<StudentNote>> getAllStudentNotes(@PathVariable String studentID) { 
    	logger.debug("getAllStudentNotes : ");
        return response.GET(commonService.getAllStudentNotes(UUID.fromString(studentID)));
    }
    
    @PostMapping (EducGradStudentApiConstants.STUDENT_NOTES_MAPPING)
    @PreAuthorize(PermissionsConstants.CREATE_OR_UPDATE_GRAD_STUDENT_NOTES_DATA)
    @Operation(summary = "Create Student Notes", description = "Create Student Notes", tags = { "Student Notes" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<ApiResponseModel<StudentNote>> saveStudentNotes(@RequestBody StudentNote studentNote) {
        logger.debug("Create student Grad Note for PEN: {}", studentNote.getStudentID());
        validation.requiredField(studentNote.getStudentID(), "Pen");
        return response.UPDATED(commonService.saveStudentNote(studentNote));
    }
    
    @DeleteMapping(EducGradStudentApiConstants.STUDENT_NOTES_DELETE_MAPPING)
    @PreAuthorize(PermissionsConstants.DELETE_GRAD_STUDENT_NOTES_DATA)
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
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find all Student Status", description = "Get all Student Status", tags = {"Student Status"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<StudentStatus>> getAllStudentStatusCodeList() {
        logger.debug("getAllUngradReasonCodeList : ");
        return response.GET(commonService.getAllStudentStatusCodeList());
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_STATUS_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find a Student Status by Student Status Code",
            description = "Get a Student Status by Student Status Code", tags = {"Student Status"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<StudentStatus> getSpecificStudentStatusCode(@PathVariable String statusCode) {
        logger.debug("getSpecificUngradReasonCode : ");
        return response.GET(commonService.getSpecificStudentStatusCode(statusCode));
    }

    @PostMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_STATUS_MAPPING)
    @PreAuthorize(PermissionsConstants.CREATE_STUDENT_STATUS)
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
    @PreAuthorize(PermissionsConstants.UPDATE_STUDENT_STATUS)
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
    @PreAuthorize(PermissionsConstants.DELETE_STUDENT_STATUS)
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
    @PreAuthorize(PermissionsConstants.STUDENT_ALGORITHM_DATA)
    @Operation(summary = "Find Student Grad Status by Student ID for algorithm", description = "Get Student Grad Status by Student ID for algorithm", tags = { "Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
    public ResponseEntity<GradStudentAlgorithmData> getStudentGradStatusForAlgorithm(@PathVariable String studentID,
                                                                                     @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Get Student Grad Status for studentID");
        return response.GET(commonService.getGradStudentAlgorithmData(studentID,accessToken.replace(BEARER, "")));
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_HISTORY_ACTIVITY_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_HISTORY_ACTIVITY)
    @Operation(summary = "Find all History Activity Codes", description = "Get all History Activity codes", tags = {"History Activity"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<HistoryActivity>> getAllHistoryActivityCodeList() {
        logger.debug("getAllHistoryActivityCodeList : ");
        return response.GET(commonService.getAllHistoryActivityCodeList());
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_HISTORY_ACTIVITY_BY_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_HISTORY_ACTIVITY)
    @Operation(summary = "Find a History Activity Code by Code",
            description = "Find a History Activity Code by Code", tags = {"History Activity"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<HistoryActivity> getSpecificHistoryActivityCode(@PathVariable String activityCode) {
        logger.debug("getSpecificHistoryActivityCode : ");
        return response.GET(commonService.getSpecificHistoryActivityCode(activityCode));
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_GRADE_CODES)
    @PreAuthorize(PermissionsConstants.READ_STUDENT_GRADE_CODES)
    @Transactional(readOnly = true)
    @Operation(summary = "Find all active Student Grade Codes",
            description = "Find all active Student Grade Codes", tags = {"Student Grade Code"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<StudentGradeCode>> getAllStudentGradeCodes() {
        logger.debug("getAllStudentGradeCodes : ");
        return response.GET(commonService.getAllStudentGradeCodes());
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_REPORT_DATA_BY_SCHOOL_ID)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find a Student Graduation Data by Institute School Id",
            description = "Find a Student Graduation Data by Institute School Id", tags = {"Student Graduation Data for School Reports"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<ReportGradStudentData>> getStudentReportDataBySchoolId(@PathVariable UUID schoolId) {
        logger.debug("getStudentReportDataByMincode : {}", schoolId);
        return response.GET(gradStudentReportService.getGradStudentDataBySchoolId(schoolId));
    }

    @PostMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_REPORT_DATA)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find a Student Graduation Data by List of GUIDs",
            description = "Find a Student Graduation Data by List of GUIDs", tags = {"Student Graduation Data for School Reports"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<ReportGradStudentData>> getStudentReportData(@RequestBody List<UUID> studentIds) {
        logger.debug("getStudentReportData :");
        return response.GET(gradStudentReportService.getGradStudentDataByStudentGuids(studentIds));
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_NON_GRAD_REPORT_DATA)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find a Student Graduation Data for Year End School Report",
            description = "Find a Student Graduation Data for Year End School Report", tags = {"Student Graduation Data for School Reports"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<ReportGradStudentData>> getStudentReportDataForYearEndNonGrad() {
        logger.debug("getStudentReportDataForYearEndNonGrad :");
        return response.GET(gradStudentReportService.getGradStudentDataForNonGradYearEndReport());
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_NON_GRAD_REPORT_DATA_BY_SCHOOL_ID)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find a Student Graduation Data for Year End School Report by School Id",
            description = "Find a Student Graduation Data for Year End School Report by School Id", tags = {"Student Graduation Data for School Reports"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<ReportGradStudentData>> getStudentReportDataForYearEndNonGrad(@PathVariable UUID schoolId) {
        logger.debug("getStudentReportDataForYearEndNonGrad By School");
        return response.GET(gradStudentReportService.getGradStudentDataForNonGradYearEndReportBySchool(schoolId));
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_STUDENT_NON_GRAD_REPORT_DATA_BY_DISTRICT_ID)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find a Student Graduation Data for Year End School Report by District Id",
            description = "Find a Student Graduation Data for Year End School Report by District Id", tags = {"Student Graduation Data for School Reports"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<ReportGradStudentData>> getStudentReportDataForYearEndNonGradByDistrict(@PathVariable UUID districtId) {
        logger.debug("getStudentReportDataForYearEndNonGrad By District:");
        return response.GET(gradStudentReportService.getGradStudentDataForNonGradYearEndReportByDistrict(districtId));
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_SCHOOL_NON_GRAD_REPORT_DATA)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find Schools for Year End School Report",
            description = "Find Schools for Year End School Report", tags = {"Schools for Year End School Reports"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<UUID>> getSchoolReportDataForYearEndNonGrad() {
        logger.debug("getSchoolReportDataForYearEndNonGrad :");
        return response.GET(gradStudentReportService.getGradSchoolsForNonGradYearEndReport());
    }

    @GetMapping(EducGradStudentApiConstants.GET_ALL_DISTRICT_NON_GRAD_REPORT_DATA)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find Schools for Year End School Report",
            description = "Find Schools for Year End School Report", tags = {"Schools for Year End School Reports"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<UUID>> getDistrictReportDataForYearEndNonGrad(@RequestHeader(name="Authorization") String accessToken) {
        logger.debug("getSchoolReportDataForYearEndNonGrad :");
        return response.GET(gradStudentReportService.getGradDistrictsForNonGradYearEndReport(accessToken.replace(BEARER, "")));
    }

    @PostMapping(EducGradStudentApiConstants.GET_DECEASED_STUDENT_ID)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_STATUS)
    @Operation(summary = "Find Deceased Student IDs by List of GUIDs",
            description = "Find Deceased Student IDs by List of GUIDs", tags = {"Student Graduation Data for School Reports"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "204", description = "NO CONTENT.")})
    public ResponseEntity<List<UUID>> getDeceasedStudentIDs(@RequestBody List<UUID> studentIds) {
        logger.debug("getDeceasedStudentIDs :");
        return response.GET(commonService.getDeceasedStudentIDs(studentIds));
    }

    @PostMapping (EducGradStudentApiConstants.GRAD_STUDENT_OPTIONAL_PROGRAM_BY_ID)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Create a Student Optional Program by Student ID and Optional Program ID", description = "Create a Student Optional Program by Student ID and Optional Program ID", tags = { "Optional Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "422", description = "VALIDATION ERROR")
    })
    public ResponseEntity<ApiResponseModel<GraduationStudentRecord>> saveStudentOptionalProgram(@PathVariable UUID studentID, @PathVariable UUID optionalProgramID, @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Create a Student Optional Program for Student ID: {}, OptionalProgram ID: {}", studentID, optionalProgramID);
        graduationStatusService.createStudentOptionalProgram(studentID, optionalProgramID, accessToken.replace(BEARER, ""));
        return response.UPDATED(graduationStatusService.getGraduationStatus(studentID, accessToken.replace(BEARER, "")));
    }

    @PostMapping (EducGradStudentApiConstants.GRAD_STUDENT_CAREER_PROGRAMS)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Create Student Career Programs by Student ID with Career Program Codes", description = "Create Student Career Programs by Student ID with Career Program Codes", tags = { "Optional Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "422", description = "VALIDATION ERROR")
    })
    public ResponseEntity<ApiResponseModel<GraduationStudentRecord>> saveStudentCareerPrograms(@PathVariable UUID studentID, @NotNull @Valid @RequestBody StudentCareerProgramRequestDTO studentCareerProgramReq,
                                                                             @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Create Student Career Programs for Student ID: {}", studentID);
        validation.requiredField(studentCareerProgramReq.getCareerProgramCodes(), "Career Program Codes");
        if (validation.hasErrors()) {
            validation.stopOnErrors();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        graduationStatusService.createStudentCareerPrograms(studentID, studentCareerProgramReq, accessToken.replace(BEARER, ""));
        return response.UPDATED(graduationStatusService.getGraduationStatus(studentID, accessToken.replace(BEARER, "")));
    }

    @DeleteMapping (EducGradStudentApiConstants.GRAD_STUDENT_OPTIONAL_PROGRAM_BY_ID)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Delete a Student Optional Program by Student ID and Optional Program ID", description = "Delete a Student Optional Program by Student ID and Optional Program ID", tags = { "Optional Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "422", description = "VALIDATION ERROR")
    })
    public ResponseEntity<ApiResponseModel<GraduationStudentRecord>> deleteStudentOptionalProgram(@PathVariable UUID studentID, @PathVariable UUID optionalProgramID, @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Delete a Student Optional Program for Student ID: {}, OptionalProgram ID: {}", studentID, optionalProgramID);
        graduationStatusService.deleteStudentOptionalProgram(studentID, optionalProgramID, accessToken.replace(BEARER, ""));
        return response.UPDATED(graduationStatusService.getGraduationStatus(studentID, accessToken.replace(BEARER, "")));
    }

    @DeleteMapping (EducGradStudentApiConstants.GRAD_STUDENT_CAREER_PROGRAMS_BY_CODE)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT_OPTIONAL_PROGRAM)
    @Operation(summary = "Delete a Student Career Program by Student ID and Career Program Code", description = "Delete a Student Career Program by Student ID and Career Program Code", tags = { "Optional Student Graduation Status" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND"),
            @ApiResponse(responseCode = "422", description = "VALIDATION ERROR")
    })
    public ResponseEntity<ApiResponseModel<GraduationStudentRecord>> deleteStudentCareerProgram(@PathVariable UUID studentID, @PathVariable String careerProgramCode, @RequestHeader(name="Authorization") String accessToken) {
        logger.debug("Delete a Student Career Program for Student ID: {}, Career Program: {}", studentID, careerProgramCode);
        graduationStatusService.deleteStudentCareerProgram(studentID, careerProgramCode, accessToken.replace(BEARER, ""));
        return response.UPDATED(graduationStatusService.getGraduationStatus(studentID, accessToken.replace(BEARER, "")));
    }
}
