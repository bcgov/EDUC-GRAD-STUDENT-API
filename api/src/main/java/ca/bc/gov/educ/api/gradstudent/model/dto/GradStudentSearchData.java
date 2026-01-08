package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradStudentSearchData {
    String studentID;
    String pen;
    String legalFirstName;
    String legalLastName;
    String legalMiddleNames;
    String dob;
    String genderCode;
    String studentGrade;
    String program;
    String programCompletionDate;
    String schoolOfRecordId;
    String schoolAtGraduationId;
    String studentStatus;
    String adultStartDate;
    String recalculateGradStatus;
    String recalculateProjectedGrad;
}
