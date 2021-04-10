package ca.bc.gov.educ.api.gradstudent.dto;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GraduationStatus {

    private String studentGradData;
    private String pen;
    private String program;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus;   
    private String schoolOfRecord;
    private String studentGrade;	
    private String studentStatus;
				
}
