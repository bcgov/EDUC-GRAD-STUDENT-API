package ca.bc.gov.educ.api.gradstudent.model.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AssessmentCompletionCurrentStudentProjection {
  UUID getGraduationStudentRecordId();
  UUID getSchoolOfRecordId();
  String getPen();
  String getLastName();
  String getFirstName();
  String getMiddleName();
  LocalDateTime getDob();
  String getStudentGrade();
  String getProgramCode();
}
