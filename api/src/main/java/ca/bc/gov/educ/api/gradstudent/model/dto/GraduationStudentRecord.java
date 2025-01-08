package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.DEFAULT_DATE_FORMAT;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GraduationStudentRecord extends BaseModel {

    private String studentGradData;
    private String pen;
    private String program;
    private String programName;
    private String programCompletionDate;
    private String gpa;
    private String honoursStanding;
    private String recalculateGradStatus;   
    private UUID schoolOfRecordId;
    private String schoolName;
    private String studentGrade;
    private String studentStatus;
    private String studentStatusName;
    private UUID studentID;
    private UUID schoolAtGradId;
    private String schoolAtGradName;
    private String recalculateProjectedGrad;
    private Long batchId;
    private String consumerEducationRequirementMet;
    private String studentCitizenship;
    private String studentProjectedGradData;
    private String legalFirstName;
    private String legalMiddleNames;
    private String legalLastName;
    @JsonFormat(pattern=DEFAULT_DATE_FORMAT)
    private Date adultStartDate;

    private List<StudentCareerProgram> careerPrograms;
    private List<StudentOptionalProgram> optionalPrograms;
    private List<GradRequirement> nonGradReasons;
}
