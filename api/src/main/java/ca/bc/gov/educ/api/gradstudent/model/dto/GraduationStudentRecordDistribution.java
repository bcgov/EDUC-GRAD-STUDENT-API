package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GraduationStudentRecordDistribution extends BaseModel{

    private UUID studentID;
    private String pen;
    private String legalFirstName;
    private String legalMiddleNames;
    private String legalLastName;
    private String schoolOfRecord;
    private String schoolAtGrad;
    private String programCompletionDate;
    private String honoursStanding;
    private String program;
    private String studentGrade;
    private String studentCitizenship;
    private List<GradRequirement> nonGradReasons;
}
