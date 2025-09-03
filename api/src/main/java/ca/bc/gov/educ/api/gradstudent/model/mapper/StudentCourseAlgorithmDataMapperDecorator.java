package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.external.algorithm.v1.StudentCourseAlgorithmData;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class StudentCourseAlgorithmDataMapperDecorator implements StudentCourseAlgorithmDataMapper {
  private final StudentCourseAlgorithmDataMapper delegate;

  protected StudentCourseAlgorithmDataMapperDecorator(StudentCourseAlgorithmDataMapper delegate) {
    this.delegate = delegate;
  }

  @Override
  public StudentCourseAlgorithmData toStructure(StudentCourseEntity entity) {
    final var studentCourseAlgorithmData = this.delegate.toStructure(entity);
    studentCourseAlgorithmData.setSessionDate(getSessionDate(entity.getCourseSession()));
    return  studentCourseAlgorithmData;
  }

  public String getSessionDate(String sessionDate) {
    if (sessionDate == null || sessionDate.trim().isEmpty()) {
      return null;
    }
    try {
      // Convert yyyyMM to yyyy/MM format
      if (sessionDate.length() == 6) {
        return sessionDate.substring(0, 4) + "/" + sessionDate.substring(4, 6);
      }
      log.error("Invalid session date format: {}", sessionDate);
      throw new IllegalArgumentException("Session date must be in yyyyMM format, got: " + sessionDate);
    } catch (Exception e) {
      log.error("Error converting session date: {}", sessionDate, e);
      throw e;
    }
  }
}
