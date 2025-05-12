package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourseHistory extends BaseModel {

    private UUID id;
    private String activityCode;
    private String activityDescription;
    private String courseID;
    private String courseSession;
    private Integer interimPercent;
    private String interimLetterGrade;
    private Integer finalPercent;
    private String finalLetterGrade;
    private Integer credits;
    private String equivOrChallenge;
    private String fineArtsAppliedSkills;
    private String customizedCourseName;
    private UUID studentExamId;
    private String relatedCourseId;

}
