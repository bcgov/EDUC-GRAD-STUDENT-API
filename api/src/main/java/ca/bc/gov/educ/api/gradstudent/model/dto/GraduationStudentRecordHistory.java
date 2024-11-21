package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.DEFAULT_DATE_FORMAT;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GraduationStudentRecordHistory extends BaseModel{

	private UUID historyID;
	private String activityCode;
    private String activityCodeDescription;
    private String studentGradData;
    private String pen;
    private String program;
    private String programName;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus;   
    private String schoolOfRecord;
    private UUID schoolOfRecordId;
    private String studentGrade;	
    private String studentStatus;
    private UUID studentID;
    private String schoolAtGrad;
    private UUID schoolAtGradId;
    private String recalculateProjectedGrad;
    private Long batchId;
    private String consumerEducationRequirementMet;
    private String studentCitizenship;
    private String studentProjectedGradData;
    @JsonFormat(pattern=DEFAULT_DATE_FORMAT)
    private Date adultStartDate;
}
