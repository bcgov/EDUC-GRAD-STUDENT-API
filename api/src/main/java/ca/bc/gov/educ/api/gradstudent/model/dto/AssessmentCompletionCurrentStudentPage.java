package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.util.List;

@Value
@Builder
public class AssessmentCompletionCurrentStudentPage implements Serializable {
  List<AssessmentCompletionCurrentStudent> content;
  int pageNumber;
  int pageSize;
  int numberOfElements;
  boolean hasNext;
}
