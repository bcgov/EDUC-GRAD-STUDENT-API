package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.Date;

@Data
@Component
public class StudentStatus {

	private String code; 
	private String description;	
	private String createdBy;
	private Date createdTimestamp;	
	private String updatedBy;	
	private Date updatedTimestamp;


}
