package ca.bc.gov.educ.api.gradstudent.model.dto.external.coreg.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseCodeRecord {
    private String courseID;
    private String externalCode;
    private String originatingSystem;

    // for testing
    public CourseCodeRecord() {

    }
}
