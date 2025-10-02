package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.HistoricStudentActivity;
import ca.bc.gov.educ.api.gradstudent.service.HistoricStudentActivityService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.PermissionsConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@CrossOrigin
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Historic Student Activity.", description = "This API is for reading historic student activity data.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_DATA"})})
@RequiredArgsConstructor
@Slf4j
public class HistoricStudentActivityController {

    private final HistoricStudentActivityService historicStudentActivityService;

    @GetMapping(EducGradStudentApiConstants.HISTORIC_STUDENT_ACTIVITY_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_GRADUATION_STUDENT)
    @Operation(summary = "Get historic student activities", description = "Retrieve historic student activities by studentID", tags = { "Historic Student Activity" })
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "BAD REQUEST"),
        @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR")
    })
    public List<HistoricStudentActivity> getHistoricStudentActivities(@PathVariable UUID studentID) {
        log.debug("getHistoricStudentActivities: studentID = {}", studentID);
        return historicStudentActivityService.getHistoricStudentActivities(studentID);
    }
}
