package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradTraxStudent {
  private String pen;
  private String program; // inc
  private Date programCompletionDate; // inc
  private String slpDate; // inc
  private String sccDate;
  private String gpa;
  private String honoursStanding; // inc
  private String studentGradData;
  private String schoolOfRecord; // inc
  private String schoolAtGrad; // inc
  private String studentGrade; // inc
  private String studentStatus; // inc
  private String archiveFlag; // inc
  private String frenchCert;
  private String englishCert;
  private String frenchDogwood;
  private String consumerEducationRequirementMet;
  private String studentCitizenship;

  // extra
  private String graduationRequirementYear;

  // grad or non-grad
  private Date distributionDate;
  private String transcriptSchoolCategoryCode;
  private String certificateSchoolCategoryCode;
  // 1950 "AD"
  private boolean adult19Rule;
}
