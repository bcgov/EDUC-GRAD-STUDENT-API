package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.*;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Component
@Builder
public class Course implements Serializable {

    private String courseCode;
    private String courseLevel;
    private String courseName;
    private String language;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate completionEndDate;
    private String genericCourseType;
    private String courseID;
    private Integer numCredits;
    private CourseCharacteristics courseCategory;
    private List<CourseAllowableCredits> courseAllowableCredit;

    public String getCourseCode() {
        return courseCode != null ? courseCode.trim(): null;
    }
    public String getCourseLevel() {
        return courseLevel != null ? courseLevel.trim(): null;
    }

}
