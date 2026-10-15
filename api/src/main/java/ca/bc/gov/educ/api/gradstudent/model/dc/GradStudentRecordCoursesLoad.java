package ca.bc.gov.educ.api.gradstudent.model.dc;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradStudentRecordCoursesLoad {

  private String courseCode;
  private String courseLevel;
  private String credits;
  private String creditsUsedForGrad;
  @JsonAlias("completedCourseLetterGrade")
  private String finalLetterGrade;
  @JsonAlias("completedCoursePercentage")
  private String finalPercent;
  private String sessionDate;
  private String gradReqMet;
  private String pen;
}