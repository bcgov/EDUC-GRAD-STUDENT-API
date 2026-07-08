package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Value
@Builder
public class AssessmentCompletionCurrentStudent implements Serializable {
  UUID graduationStudentRecordId;
  UUID schoolOfRecordId;
  String schoolOfRecordName;
  String schoolOfRecordMincode;
  String pen;
  String lastName;
  String firstName;
  String middleName;
  LocalDateTime dob;
  String studentGrade;
  String programCode;
}
