package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigInteger;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourse extends BaseModel {

    private UUID id;
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
    private UUID studentExamId;
    private String relatedCourseId;

}
