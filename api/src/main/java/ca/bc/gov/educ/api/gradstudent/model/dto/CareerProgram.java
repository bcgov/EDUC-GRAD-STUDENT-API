package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class CareerProgram extends BaseModel {
	
	private String code; 
	private String description; 
	private String startDate; 
	private String endDate;	
}
