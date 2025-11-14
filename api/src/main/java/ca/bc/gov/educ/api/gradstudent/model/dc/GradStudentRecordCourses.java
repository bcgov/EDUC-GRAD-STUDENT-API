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
public class GradStudentRecordCourses {

    private String courseCode;
    private String courseLevel;
    @JsonProperty("sessionDate")
    private String courseSession;
    private String gradReqMet;
}
