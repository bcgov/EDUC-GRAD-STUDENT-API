package ca.bc.gov.educ.api.gradstudent.controller;

import ca.bc.gov.educ.api.gradstudent.model.dto.EdwGraduationSnapshot;
import ca.bc.gov.educ.api.gradstudent.service.EdwSnapshotService;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.GradValidation;
import ca.bc.gov.educ.api.gradstudent.util.PermissionsConstants;
import ca.bc.gov.educ.api.gradstudent.util.ResponseHelper;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(EducGradStudentApiConstants.GRAD_STUDENT_API_ROOT_MAPPING)
@OpenAPIDefinition(info = @Info(title = "API for Grad Status Snapshot for EDW.", description = "This API is for Grad Status Snapshot for EDW.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT_DATA", "UPDATE_GRAD_GRADUATION_STATUS"})})
public class EdwSnapshotController {

    private static final Logger logger = LoggerFactory.getLogger(EdwSnapshotController.class);

    @Autowired
    EdwSnapshotService edwSnapshotService;

    @Autowired
    GradValidation validation;

    @Autowired
    ResponseHelper response;

    @PostMapping(EducGradStudentApiConstants.EDW_GRADUATION_STATUS_SNAPSHOT)
    @PreAuthorize(PermissionsConstants.UPDATE_GRADUATION_STUDENT)
    @Operation(summary = "Save Graduation Status Snapshot for EDW", description = "Save Graduation Status Snapshot for EDW", tags = { "EDW Snapshot" })
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    public ResponseEntity<EdwGraduationSnapshot> saveGradStatusForEDW(@RequestBody EdwGraduationSnapshot edwGraduationSnapshot) {
        logger.debug("Save Graduation Status Snapshot for EDW");
        var result = edwSnapshotService.saveEdwGraduationSnapshot(edwGraduationSnapshot);
        return response.GET(result);
    }

}
