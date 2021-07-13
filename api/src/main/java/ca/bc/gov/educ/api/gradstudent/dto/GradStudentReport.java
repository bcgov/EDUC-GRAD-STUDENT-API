package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class GradStudentReport extends BaseModel {

	private String pen;	
	private String studentTranscriptReport;
	private String studentAchievementReport;
}
