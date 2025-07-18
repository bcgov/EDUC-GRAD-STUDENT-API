package ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoregCoursesRecord {
    private String courseID;
    private String sifSubjectCode;
    private String courseTitle;
    private String startDate;
    private String endDate;
    private String completionEndDate;
    private String genericCourseType;
    private String programGuideTitle;
    private String externalIndicator;
    private Set<CourseCodeRecord> courseCode;
    private CourseCharacteristicsRecord courseCharacteristics;
    private CourseCharacteristicsRecord courseCategory;
    private Set<CourseAllowableCreditRecord> courseAllowableCredit;
    private Set<RequiredCourseRecord> requiredCourse;
}
