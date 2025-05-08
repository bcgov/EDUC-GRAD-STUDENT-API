package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CourseAllowableCredits implements Serializable {

    private String cacID;
    private String creditValue;
    private String courseID;
    private String startDate;
    private String endDate;

}
