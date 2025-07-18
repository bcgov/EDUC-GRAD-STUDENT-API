package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OptionalStudentAssessments {
    private List<OptionalStudentAssessment> studentAssessmentList;
}

