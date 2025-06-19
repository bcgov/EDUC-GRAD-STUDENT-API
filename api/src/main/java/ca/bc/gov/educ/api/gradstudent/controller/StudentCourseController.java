package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.service.StudentCourseService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.PermissionsConstants;
import ca.bc.gov.educ.api.gradstudent.util.ResponseHelper;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Student Courses management.", description = "This API is Student Courses.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_COURSE_DATA", "UPDATE_GRAD_STUDENT_COURSE", "DELETE_GRAD_STUDENT_COURSE"})})
@AllArgsConstructor
public class StudentCourseController {

    private static final Logger logger = LoggerFactory.getLogger(StudentCourseController.class);

    private final StudentCourseService studentCourseService;

    private final ResponseHelper response;

    @GetMapping(EducGradStudentApiConstants.STUDENT_COURSE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_COURSE)
    @Operation(summary = "Get student courses", description = "Retrieve student courses by studentID", tags = { "Student courses" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT")
    })
    public ResponseEntity<List<StudentCourse>> getStudentCourses(@PathVariable UUID studentID) {
        logger.debug("getStudentCourses: studentID = {}", studentID);
        return response.GET(studentCourseService.getStudentCourses(studentID));
    }


    @PostMapping(EducGradStudentApiConstants.STUDENT_COURSE_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_STUDENT_COURSE)
    @Operation(summary = "Create student courses", description = "Add new student courses to student", tags = { "Student courses" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT")
    })
    public ResponseEntity<List<StudentCourseValidationIssue>> createStudentCourses(@PathVariable UUID studentID, @NotNull @Valid @RequestBody List<StudentCourse> studentCourses) {
        logger.debug("createStudentCourses: studentID = {}", studentID);
        List<StudentCourseValidationIssue> results = studentCourseService.saveStudentCourses(studentID, studentCourses,false);
        return response.GET(results);
    }

    @PutMapping(EducGradStudentApiConstants.STUDENT_COURSE_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_STUDENT_COURSE)
    @Operation(summary = "Update student courses", description = "Update student courses of student", tags = { "Student courses" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT")
    })
    public ResponseEntity<List<StudentCourseValidationIssue>> updateStudentCourses(@PathVariable UUID studentID, @NotNull @Valid @RequestBody List<StudentCourse> studentCourses) {
        logger.debug("updateStudentCourses: studentID = {}", studentID);
        List<StudentCourseValidationIssue> results = studentCourseService.saveStudentCourses(studentID, studentCourses,true);
        return response.GET(results);
    }


    @DeleteMapping(EducGradStudentApiConstants.STUDENT_COURSE_MAPPING)
    @PreAuthorize(PermissionsConstants.DELETE_GRAD_STUDENT_COURSE)
    @Operation(summary = "Delete Student Courses", description = "Delete Student Courses by studentID", tags = { "Student courses" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<List<StudentCourseValidationIssue>> deleteStudentCourses(@PathVariable UUID studentID, @NotNull @Valid @RequestBody List<UUID> studentCourses) {
        logger.debug("deleteStudentCourses: studentID = {}", studentID);
        List<StudentCourseValidationIssue> results = studentCourseService.deleteStudentCourses(studentID, studentCourses);
        return response.GET(results);
    }

    @GetMapping(EducGradStudentApiConstants.STUDENT_COURSE_HISTORY_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRAD_STUDENT_COURSE)
    @Operation(summary = "Get student course history", description = "Retrieve student course history by studentID", tags = { "Student courses" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT")
    })
    public ResponseEntity<List<StudentCourseHistory>> getStudentCourseHistory(@PathVariable UUID studentID) {
        logger.debug("getStudentCourses history: studentID = {}", studentID);
        return response.GET(studentCourseService.getStudentCourseHistory(studentID));
    }

    @PostMapping(EducGradStudentApiConstants.STUDENT_COURSE_TRANSFER_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_STUDENT_COURSE)
    @Operation(summary = "Transfer student courses", description = "Transfer student courses to a different student", tags = { "Student courses" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
        @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT"),
        @ApiResponse(responseCode = "204", description = "Transfer successful, no validation issues"),
        @ApiResponse(responseCode = "200", description = "Validation issues found during transfer")
    })
    public ResponseEntity<List<ValidationIssue>> transferStudentCourses(@NotNull @Valid @RequestBody StudentCoursesTransferReq studentCoursesRequest) {
        logger.debug("transfer student courses from: studentId = {} to: studentId = {}", studentCoursesRequest.getSourceStudentId(), studentCoursesRequest.getTargetStudentId());
        var results = studentCourseService.transferStudentCourse(studentCoursesRequest);
        if (results.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(results);
    }
}
