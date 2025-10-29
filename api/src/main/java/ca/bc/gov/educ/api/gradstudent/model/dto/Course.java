package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Component
@SuperBuilder
public class Course extends BaseCourse {

    private CourseCharacteristics courseCategory;
    private List<CourseAllowableCredits> courseAllowableCredit;

}
