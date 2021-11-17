package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import javax.persistence.Column;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GraduationStudentRecord extends BaseModel{

    private String studentGradData;
    private String pen;
    private String program;
    private String programName;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus;   
    private String schoolOfRecord;
    private String schoolName;
    private String studentGrade;	
    private String studentStatus;
    private String studentStatusName;
    private UUID studentID;
    private String schoolAtGrad;
    private String schoolAtGradName;
    private String recalculateProjectedGrad;
    private String batchId;

}
