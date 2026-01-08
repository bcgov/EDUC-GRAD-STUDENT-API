package ca.bc.gov.educ.api.gradstudent.endpoint.v1;

import ca.bc.gov.educ.api.gradstudent.model.dto.DownloadableReportResponse;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.PermissionsConstants;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.UUID;

@RequestMapping( EducGradStudentApiConstants.BASE_URL_REPORT)
public interface ReportsEndpoint {

    @GetMapping("/{districtID}/download/{fromDate}/{toDate}")
    @PreAuthorize("hasAuthority('SCOPE_READ_GRAD_STUDENT_REPORT')")
    @Transactional()
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    DownloadableReportResponse getYukonReport(@PathVariable UUID districtID, @PathVariable String fromDate, @PathVariable String toDate);

    @GetMapping("/course-students/search/download")
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Transactional(readOnly = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    void getCourseStudentSearchReport(@RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson, HttpServletResponse response) throws IOException;

    @GetMapping("/program-students/search/download")
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Transactional(readOnly = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    void getProgramStudentSearchReport(@RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson, HttpServletResponse response) throws IOException;

    @GetMapping("/optional-program-students/search/download")
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Transactional(readOnly = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    void getOptionalProgramStudentSearchReport(@RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson, HttpServletResponse response) throws IOException;
}
