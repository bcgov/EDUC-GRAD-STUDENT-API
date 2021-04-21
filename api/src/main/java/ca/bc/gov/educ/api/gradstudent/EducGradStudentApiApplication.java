package ca.bc.gov.educ.api.gradstudent;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import ca.bc.gov.educ.api.gradstudent.dto.GradStudent;
import ca.bc.gov.educ.api.gradstudent.entity.GradStudentEntity;


@SpringBootApplication
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableCaching
public class EducGradStudentApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EducGradStudentApiApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.typeMap(GradStudentEntity.class, GradStudent.class);
		modelMapper.typeMap(GradStudent.class, GradStudentEntity.class);
		return modelMapper;
	}
	
	@Bean
	public WebClient webClient() {
		return WebClient.create();
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}
	
	@Configuration
	static
	class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
	  /**
	   * Instantiates a new Web security configuration.
	   * This makes sure that security context is propagated to async threads as well.
	   */
	  public WebSecurityConfiguration() {
	    super();
	    SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	  }
	  @Override
	  public void configure(WebSecurity web) {
	    web.ignoring().antMatchers("/v3/api-docs/**",
	            "/actuator/health","/actuator/prometheus",
	            "/swagger-ui/**", "/health");
	  }
	}
}
