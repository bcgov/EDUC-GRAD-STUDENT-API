package ca.bc.gov.educ.api.gradstudent.endpoint.v1;

import ca.bc.gov.educ.api.gradstudent.model.dto.DownloadableReportResponse;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.UUID;

@RequestMapping( EducGradStudentApiConstants.BASE_URL_REPORT)
public interface ReportsEndpoint {

    @GetMapping("/{districtID}/download/{fromDate}/{toDate}")
    @PreAuthorize("hasAuthority('SCOPE_READ_GRAD_STUDENT_REPORT')")
    @Transactional()
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
    DownloadableReportResponse getYukonReport(@PathVariable UUID districtID, @PathVariable String fromDate, @PathVariable String toDate);

}
