package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;

import java.sql.Date;

@Data
public class BaseModel {
	private String createdBy;	
	private Date createdTimestamp;	
	private String updatedBy;	
	private Date updatedTimestamp;
}
