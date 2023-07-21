package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
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
    private Long batchId;
    private String consumerEducationRequirementMet;
    private String studentCitizenship;
    private String studentProjectedGradData;
    private String legalFirstName;
    private String legalMiddleNames;
    private String legalLastName;
    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate adultStartDate;

    private List<StudentCareerProgram> careerPrograms;
    private List<GradRequirement> nonGradReasons;
}
