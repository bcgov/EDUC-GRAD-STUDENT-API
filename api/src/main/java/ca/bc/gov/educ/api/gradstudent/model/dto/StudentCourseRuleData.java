package ca.bc.gov.educ.api.gradstudent.model.dto;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseActivityType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourseRuleData {

    StudentCourse studentCourse;
    GraduationStudentRecord graduationStudentRecord;
    Course course;
    Course relatedCourse;
    StudentCourseActivityType activityType;
    Boolean isSystemCoordinator;

}
