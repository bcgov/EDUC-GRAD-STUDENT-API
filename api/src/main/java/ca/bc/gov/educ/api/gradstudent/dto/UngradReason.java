package ca.bc.gov.educ.api.gradstudent.dto;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class UngradReason extends BaseModel {

	private String code; 
	private String description;
	private String label;
	private String displayOrder;
	private String effectiveDate;
	private String expiryDate;	
	
}
