package ca.bc.gov.educ.api.gradstudent;

import java.time.Duration;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;

@SpringBootApplication
public class EducGradStudentApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(EducGradStudentApiApplication.class, args);
	}
	
	 @Value("${spring.security.user.name}")
	 private String uName;
	    
     @Value("${spring.security.user.password}")
     private String pass;

	@Bean
	public ModelMapper modelMapper() {

		ModelMapper modelMapper = new ModelMapper();
		modelMapper.typeMap(GradStudentEntity.class, GradStudent.class);
		modelMapper.typeMap(GradStudent.class, GradStudentEntity.class);
		return modelMapper;
	}
	
	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.basicAuthentication(uName, pass).build();
	}
}
