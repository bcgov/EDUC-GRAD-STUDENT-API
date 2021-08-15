package ca.bc.gov.educ.api.gradstudent.dto;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class CareerProgram extends BaseModel {
	
	private String code; 
	private String description; 
	private String startDate; 
	private String endDate;	
}
