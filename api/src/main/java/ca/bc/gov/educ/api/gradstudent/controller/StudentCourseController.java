package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.service.StudentCourseService;
import ca.bc.gov.educ.api.gradstudent.util.*;
import ca.bc.gov.educ.api.gradstudent.validator.rules.ValidationGroups;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.model.dc.EventOutcome.STUDENT_COURSES_UPDATED;
import static ca.bc.gov.educ.api.gradstudent.model.dc.EventType.UPDATE_STUDENT_COURSES;

@RestController
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Student Courses management.", description = "This API is Student Courses.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_COURSE_DATA", "UPDATE_GRAD_STUDENT_COURSE", "DELETE_GRAD_STUDENT_COURSE"})})
@AllArgsConstructor
public class StudentCourseController {

    private static final Logger logger = LoggerFactory.getLogger(StudentCourseController.class);

    private final StudentCourseService studentCourseService;
    private final Publisher publisher;

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
    public ResponseEntity<List<StudentCourseValidationIssue>> createStudentCourses(@PathVariable UUID studentID, @NotNull @RequestBody List<StudentCourse> studentCourses) throws JsonProcessingException {
        logger.debug("createStudentCourses: studentID = {}", studentID);
        var pairResults = studentCourseService.saveStudentCourses(studentID, studentCourses,false);
        if(pairResults.getRight() != null) {
            publisher.dispatchChoreographyEvent(pairResults.getRight());
        }
        return response.GET(pairResults.getLeft());
    }

    @PutMapping(EducGradStudentApiConstants.STUDENT_COURSE_MAPPING)
    @PreAuthorize(PermissionsConstants.UPDATE_GRAD_STUDENT_COURSE)
    @Operation(summary = "Update student courses", description = "Update student courses of student", tags = { "Student courses" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
            @ApiResponse(responseCode = "422", description = "UNPROCESSABLE CONTENT")
    })
    public ResponseEntity<StudentCourseValidationIssue> updateStudentCourses(@PathVariable UUID studentID, @NotNull @RequestBody @Validated(ValidationGroups.Update.class)  StudentCourse studentCourse) throws JsonProcessingException {
        logger.debug("updateStudentCourses: studentID = {}", studentID);
        var pairResults = studentCourseService.saveStudentCourses(studentID, List.of(studentCourse),true);
        if(pairResults.getRight() != null) {
            publisher.dispatchChoreographyEvent(pairResults.getRight());
        }
        return response.GET(pairResults.getLeft().get(0));
    }


    @DeleteMapping(EducGradStudentApiConstants.STUDENT_COURSE_MAPPING)
    @PreAuthorize(PermissionsConstants.DELETE_GRAD_STUDENT_COURSE)
    @Operation(summary = "Delete Student Courses", description = "Delete Student Courses by studentID", tags = { "Student courses" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    public ResponseEntity<List<StudentCourseValidationIssue>> deleteStudentCourses(@PathVariable UUID studentID, @NotNull @RequestBody List<UUID> studentCourses) throws JsonProcessingException {
        logger.debug("deleteStudentCourses: studentID = {}", studentID);
        var pairResults = studentCourseService.deleteStudentCourses(studentID, studentCourses);
        if(pairResults.getRight() != null) {
            publisher.dispatchChoreographyEvent(pairResults.getRight());
        }
        return response.GET(pairResults.getLeft());
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
    public ResponseEntity<List<ValidationIssue>> transferStudentCourses(@NotNull @Valid @RequestBody StudentCoursesTransferReq studentCoursesRequest) throws JsonProcessingException {
        logger.debug("transfer student courses from: studentId = {} to: studentId = {}", studentCoursesRequest.getSourceStudentId(), studentCoursesRequest.getTargetStudentId());
        var pairResults = studentCourseService.transferStudentCourse(studentCoursesRequest);
        if(pairResults.getRight() != null) {
            pairResults.getRight().forEach(publisher::dispatchChoreographyEvent);
        }
        if (pairResults.getLeft().isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(pairResults.getLeft());
    }
}
