package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourseValidationIssue {

    private UUID id; // This property is only populated when the student course has been persisted.
    private String courseID;
    private String courseSession;
    private String courseCode;
    private String courseLevel;
    private boolean hasPersisted;
    private List<ValidationIssue> validationIssues;

}
