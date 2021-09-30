package ca.bc.gov.educ.api.gradstudent.model.dto;

import java.util.Date;

import org.springframework.stereotype.Component;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class HistoryActivity extends BaseModel{

	private String code; 
	private String label; 
	private int displayOrder; 
	private String description;
	private Date effectiveDate; 
	private Date expiryDate;
}
