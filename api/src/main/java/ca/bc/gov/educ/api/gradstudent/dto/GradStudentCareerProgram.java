package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GradStudentCareerProgram extends BaseModel {

	private UUID id;
	private String pen; 	
	private String careerProgramCode;	
	private String careerProgramName;
	private UUID studentID;
	
}
