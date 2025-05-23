package ca.bc.gov.educ.api.gradstudent.config;

import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private final EducGradStudentApiConstants constants;

    public SwaggerConfig(EducGradStudentApiConstants constants) {
        this.constants = constants;
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList("OAUTH2"))
                .schemaRequirement("OAUTH2", new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .flows(new OAuthFlows()
                                .clientCredentials( new OAuthFlow()
                                        .tokenUrl(constants.getTokenUrl())
                                )));
    }
}