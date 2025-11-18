package ca.bc.gov.educ.api.gradstudent.model.dc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradStudentRecordCoursesLoad {

    private String courseCode;
    private String courseLevel;
    private String sessionDate;
    private String gradReqMet;
}
