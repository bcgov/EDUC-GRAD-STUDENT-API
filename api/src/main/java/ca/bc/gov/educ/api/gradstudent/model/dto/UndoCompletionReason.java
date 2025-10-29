package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class UndoCompletionReason extends BaseModel {

	private String code; 
	private String description;
	private String label;
	private String displayOrder;
	private String effectiveDate;
	private String expiryDate;	
	
}
