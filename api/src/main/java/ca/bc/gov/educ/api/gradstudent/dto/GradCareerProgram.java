package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.sql.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GradCareerProgram extends BaseModel {
	
	private String code; 
	private String description; 
	private Date startDate; 
	private Date endDate;	
}
