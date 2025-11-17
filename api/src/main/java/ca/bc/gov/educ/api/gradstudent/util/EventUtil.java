package ca.bc.gov.educ.api.gradstudent.util;


import ca.bc.gov.educ.api.gradstudent.constant.EventOutcome;
import ca.bc.gov.educ.api.gradstudent.constant.EventStatus;
import ca.bc.gov.educ.api.gradstudent.constant.EventType;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseUpdate;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStatusEvent;

import java.time.LocalDateTime;
import java.util.List;


public class EventUtil {
  private EventUtil() {
  }

  public static GradStatusEvent createEvent(String createUser, String updateUser, String jsonString, EventType eventType, EventOutcome eventOutcome) {
    return GradStatusEvent.builder()
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .createUser(createUser)
      .updateUser(updateUser)
      .eventPayload(jsonString)
      .eventType(eventType.toString())
      .eventStatus(EventStatus.DB_COMMITTED.toString())
      .eventOutcome(eventOutcome.toString())
      .build();
  }

  public static StudentCourseUpdate getStudentCourseUpdate(String studentID, List<StudentCourse> courses){
    StudentCourseUpdate studentCourseUpdate = new StudentCourseUpdate();
    studentCourseUpdate.setStudentID(studentID);
    studentCourseUpdate.setStudentCourses(courses);
    return studentCourseUpdate;
  }
}
