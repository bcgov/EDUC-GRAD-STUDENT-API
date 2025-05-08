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
public class StudentCourseResponse  {

    private UUID id;
    private boolean hasErrors;
    private List<StudentCourseValidationIssue> validationIssues;

}
