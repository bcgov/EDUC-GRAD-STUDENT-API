package ca.bc.gov.educ.api.gradstudent.dto;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
@Component
public class OptionalProgram extends BaseModel {

	private UUID optionalProgramID; 
	private String optProgramCode; 
	private String optionalProgramName;
	private String description; 
	private int displayOrder; 
	private Date effectiveDate;
	private Date expiryDate;
	private String graduationProgramCode;
	
	
			
}
