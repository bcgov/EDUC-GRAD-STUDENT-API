package ca.bc.gov.educ.api.gradstudent.model.dc;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class GradStudentCourseRecordsPayload {
    private List<StudentCourse> courses;
    private String exception;
}
