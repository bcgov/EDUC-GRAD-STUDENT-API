package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.FineArtsAppliedSkillsCode;
import ca.bc.gov.educ.api.gradstudent.service.FineArtsAppliedSkillsCodeService;
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
@OpenAPIDefinition(info = @Info(title = "API for Fine Arts Applied Skills Codes.", description = "This API is Fine Arts Applied Skills Codes.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_FINE_ART_APPLIED_SKILLS_CODE"})})
@AllArgsConstructor
public class FineArtsAppliedSkillsCodeController {

    private final FineArtsAppliedSkillsCodeService fineArtsAppliedSkillsCodeService;

    @GetMapping(EducGradStudentApiConstants.FINE_ART_APPLIED_SKILLS_CODES_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_FINE_ART_APPLIED_SKILLS_CODE)
    @Operation(summary = "Find All Fine Arts Applied Skills Codes", description = "Find All Fine Arts Applied Skills Codes", tags = {"Fine Arts Applied Skills Code"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<FineArtsAppliedSkillsCode>> getFineArtsAppliedSkillsCodes() {
        return ResponseEntity.ok().body(fineArtsAppliedSkillsCodeService.findAll());
    }

    @GetMapping(EducGradStudentApiConstants.FINE_ART_APPLIED_SKILLS_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_FINE_ART_APPLIED_SKILLS_CODE)
    @Operation(summary = "Find Fine Arts Applied Skills Code", description = "Find Fine Arts Applied Skills Code", tags = {"Fine Arts Applied Skills Code"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    public ResponseEntity<FineArtsAppliedSkillsCode> getFineArtsAppliedSkillsCode(@PathVariable String fineArtsAppliedSkillsCode) {
        return ResponseEntity.ok().body(fineArtsAppliedSkillsCodeService.findByFineArtsAppliedSkillsCode(fineArtsAppliedSkillsCode));
    }
}
