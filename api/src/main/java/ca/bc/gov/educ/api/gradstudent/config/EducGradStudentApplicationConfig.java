package ca.bc.gov.educ.api.gradstudent.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = {"ca.bc.gov.educ.api.gradstudent.model.entity"} )
@EnableJpaRepositories(basePackages = {"ca.bc.gov.educ.api.gradstudent.repository"})
public class EducGradStudentApplicationConfig {
}
