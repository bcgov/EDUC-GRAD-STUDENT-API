package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.EquivalentOrChallengeCode;
import ca.bc.gov.educ.api.gradstudent.service.EquivalentOrChallengeCodeService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Equivalent Or Challenge Codes.", description = "This API is Equivalent Or Challenge Codes.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_EQUIVALENT_OR_CHALLENGE_CODE"})})
@AllArgsConstructor
public class EquivalentOrChallengeCodeController {

    private final EquivalentOrChallengeCodeService equivalentOrChallengeCodeService;

    @GetMapping(EducGradStudentApiConstants.EQUIVALENT_OR_CHALLENGE_CODES_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_EQUIVALENT_OR_CHALLENGE_CODE)
    @Operation(summary = "Find All Equivalent Or Challenge Codes", description = "Find All Equivalent Or Challenge Codes", tags = {"Equivalent Or Challenge Code"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<List<EquivalentOrChallengeCode>> getEquivalentOrChallengeCodes() {
        return ResponseEntity.ok().body(equivalentOrChallengeCodeService.findAll());
    }

    @GetMapping(EducGradStudentApiConstants.EQUIVALENT_OR_CHALLENGE_CODE_MAPPING)
    @PreAuthorize(PermissionsConstants.READ_EQUIVALENT_OR_CHALLENGE_CODE)
    @Operation(summary = "Find Equivalent Or Challenge Code", description = "Find Equivalent Or Challenge Code", tags = {"Equivalent Or Challenge Code"})
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    public ResponseEntity<EquivalentOrChallengeCode> getEquivalentOrChallengeCode(@PathVariable String equivalentOrChallengeCode) {
        return ResponseEntity.ok().body(equivalentOrChallengeCodeService.findByEquivalentOrChallengeCode(equivalentOrChallengeCode));
    }
}
