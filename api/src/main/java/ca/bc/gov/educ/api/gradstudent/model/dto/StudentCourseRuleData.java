package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourseRuleData {

    StudentCourse studentCourse;
    GraduationStudentRecord graduationStudentRecord;
    Course course;
    LetterGrade interimLetterGrade;
    LetterGrade finalLetterGrade;
    List<ExaminableCourse> examinableCourses;

}
