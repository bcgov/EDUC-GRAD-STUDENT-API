package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class OptionalStudentCourses {
    private List<OptionalStudentCourse> studentCourseList;

    @Override
    public String toString() {
        StringBuffer output = new StringBuffer("");

        for (OptionalStudentCourse sc : studentCourseList) {
            output.append(sc.toString())
                    .append("\n");
        }
        return output.toString();
    }
}
