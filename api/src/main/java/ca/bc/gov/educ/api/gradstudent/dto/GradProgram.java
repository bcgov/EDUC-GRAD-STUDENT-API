package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
public class GradProgram extends BaseModel {

	private String programCode; 
	private String programName; 
	private String programType;	
	
	@Override
	public String toString() {
		return "GradProgram [programCode=" + programCode + ", programName=" + programName + ", programType="
				+ programType + "]";
	}
	
	
			
}
