package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
    private String schoolName;
    private String schoolAtGrad;
    private String schoolAtGradName;
    private String studentGrade;	
    private String studentStatus;
    private String studentStatusName;
    private UUID studentID;
				
}
