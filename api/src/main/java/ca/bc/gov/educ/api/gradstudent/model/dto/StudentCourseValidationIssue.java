package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourseValidationIssue {

    private String courseID;
    private String courseSession;
    private boolean hasPersisted;
    private List<ValidationIssue> validationIssues;

}
