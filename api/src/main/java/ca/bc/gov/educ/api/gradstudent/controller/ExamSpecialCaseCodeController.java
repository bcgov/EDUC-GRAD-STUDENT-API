package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.ExamSpecialCaseCode;
import ca.bc.gov.educ.api.gradstudent.service.ExamSpecialCaseCodeService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.PermissionsConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Exam Special case Codes.", description = "This API is Exam Special case Codes.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_EXAM_SPECIAL_CASE_CODE"})})
@AllArgsConstructor
public class ExamSpecialCaseCodeController {

    private final ExamSpecialCaseCodeService examSpecialCaseCodeService;

    @GetMapping(EducGradStudentApiConstants.EXAM_SPECIAL_CASE_CODES_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_EXAM_SPECIAL_CASE_CODE)
    @Operation(summary = "Find All Exam Special Case Codes", description = "Find All Exam Special Case Codes", tags = {"Exam Special Case Code"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<ExamSpecialCaseCode>> getExamSpecialCaseCodes() {
        return ResponseEntity.ok().body(examSpecialCaseCodeService.findAll());
    }

    @GetMapping(EducGradStudentApiConstants.EXAM_SPECIAL_CASE_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_EXAM_SPECIAL_CASE_CODE)
    @Operation(summary = "Find Exam Special Case Code", description = "Find Exam Special Case Code", tags = {"Exam Special Case Code"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    public ResponseEntity<ExamSpecialCaseCode> getExamSpecialCaseCode(@PathVariable String examSpecialCaseCode) {
        return ResponseEntity.ok().body(examSpecialCaseCodeService.findBySpecialCaseCode(examSpecialCaseCode));
    }
}
