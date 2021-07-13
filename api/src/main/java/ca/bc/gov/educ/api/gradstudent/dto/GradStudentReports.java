package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class GradStudentReports extends BaseModel {

	private UUID id;	
	private String pen;	
	private String report;
	private String gradReportTypeCode;
	private UUID studentID;
}
