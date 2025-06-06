package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class CourseAllowableCredits implements Serializable {

    private String cacID;
    private String creditValue;
    private String courseID;
    private String startDate;
    private String endDate;

}
