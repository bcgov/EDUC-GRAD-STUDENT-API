package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.endpoint.v1.ReportsEndpoint;
import ca.bc.gov.educ.api.gradstudent.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.gradstudent.exception.errors.ApiError;
import ca.bc.gov.educ.api.gradstudent.model.dto.DownloadableReportResponse;
import ca.bc.gov.educ.api.gradstudent.service.CSVReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ReportsController implements ReportsEndpoint {

    private final CSVReportService csvReportService;

    @Override
    public DownloadableReportResponse getYukonReport(UUID districtID, String fromDate, String toDate) {

        if(districtID == null || StringUtils.isBlank(fromDate) || StringUtils.isBlank(toDate)) {
            ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
            throw new InvalidPayloadException(error);
        }
        return csvReportService.generateYukonReport(districtID, fromDate, toDate);
    }

    @Override
    public void getCourseStudentSearchReport(String searchCriteriaListJson, HttpServletResponse response) throws IOException {
        csvReportService.generateCourseStudentSearchReportStream(searchCriteriaListJson, response);
    }

    @Override
    public void getProgramStudentSearchReport(String searchCriteriaListJson, HttpServletResponse response) throws IOException {
        csvReportService.generateProgramStudentSearchReportStream(searchCriteriaListJson, response);
    }
}
