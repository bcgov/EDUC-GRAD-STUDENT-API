package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
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
public class StudentCourse extends BaseModel {

    private UUID id; //The property id is used only for update
    @NotBlank
    private String courseID;
    @NotBlank
    private String courseSession;
    private Integer interimPercent;
    private String interimLetterGrade;
    private Integer finalPercent;
    private String finalLetterGrade;
    private Integer credits;
    private String equivOrChallenge;
    private String fineArtsAppliedSkills;
    private String customizedCourseName;
    private String relatedCourseID;
    private StudentCourseExam courseExam;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String courseCode;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String courseLevel;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String relatedCourseCode;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String relatedCourseLevel;
}
