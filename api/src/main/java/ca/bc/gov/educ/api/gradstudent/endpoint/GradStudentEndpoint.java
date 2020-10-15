package ca.bc.gov.educ.api.gradstudent.endpoint;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/")
@OpenAPIDefinition(info = @Info(title = "API for Grad Student.",
        description = "This Read API is for Reading data of a student in BC from open vms system.", version = "1"),
        security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_GRAD_STUDENT"})})
public interface GradStudentEndpoint {

    /**
     * Gets Student details by pen.
     *
     * @param pen the pen
     * @return the student details by pen
     */
    @GetMapping("/api/v1/{pen}")
    //@PreAuthorize("#oauth2.hasScope('READ_GRAD_STUDENT')")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "NOT FOUND.")})
    GradStudentEntity getGradStudentByPen(@PathVariable String pen);

}
