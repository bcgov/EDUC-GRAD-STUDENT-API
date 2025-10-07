package ca.bc.gov.educ.api.gradstudent.model.dto;

import ca.bc.gov.educ.api.gradstudent.validator.rules.ValidationGroups;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourse extends BaseModel {

    @NotBlank(message = "id is required and cannot be blank", groups = ValidationGroups.Update.class)
    private String id; //The property id is used only for update & rendering back on read.
    @NotBlank(message = "Course ID is required and cannot be blank")
    private String courseID;
    @NotBlank(message = "Course Session is required and cannot be blank")
    private String courseSession;
    private Integer interimPercent;
    private String interimLetterGrade;
    private Integer finalPercent;
    private String finalLetterGrade;
    @NotNull(message = "credits cannot be null")
    private Integer credits;
    private String equivOrChallenge;
    private String fineArtsAppliedSkills;
    private String customizedCourseName;
    private String relatedCourseId;
    private StudentCourseExam courseExam;

}
