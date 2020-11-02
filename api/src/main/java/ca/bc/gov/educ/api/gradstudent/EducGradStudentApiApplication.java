package ca.bc.gov.educ.api.gradstudent;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import ca.bc.gov.educ.api.gradstudent.model.GradStudentEntity;
import ca.bc.gov.educ.api.gradstudent.struct.GradStudent;

@SpringBootApplication
//@EnableSwagger2
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
}
