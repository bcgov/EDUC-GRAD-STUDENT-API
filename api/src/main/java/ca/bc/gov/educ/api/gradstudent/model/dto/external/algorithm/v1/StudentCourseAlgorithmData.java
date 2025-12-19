package ca.bc.gov.educ.api.gradstudent.model.dto.external.algorithm.v1;

import ca.bc.gov.educ.api.gradstudent.model.dto.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourseAlgorithmData extends BaseModel {

  private String courseCode;
  private String courseName;
  private Integer originalCredits;
  private String courseLevel;
  private String genericCourseType;
  private String language;
  private String sessionDate;
  private Double interimPercent;
  private String interimLetterGrade;
  private Double finalPercent;
  private String finalLetterGrade;
  private Integer credits;
  private String equivOrChallenge;
  private String fineArtsAppliedSkills;
  private String customizedCourseName;
  private String relatedCourse;
  private String relatedCourseName;
  private String relatedLevel;
  private Double bestSchoolPercent;
  private Double bestExamPercent;
  private Double schoolPercent;
  private Double examPercent;
  private String specialCase;
  private String toWriteFlag;
  private String provExamCourse;
}
