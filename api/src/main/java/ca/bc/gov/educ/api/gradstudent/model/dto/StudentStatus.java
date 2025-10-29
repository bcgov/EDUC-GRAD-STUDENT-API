package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class StudentStatus extends BaseModel{

	private String code; 
	private String label; 
	private int displayOrder; 
	private String description;
	private Date effectiveDate; 
	private Date expiryDate;
}
