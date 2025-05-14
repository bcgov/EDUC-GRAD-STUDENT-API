package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class LetterGrade {

    private String grade;
    private String gpaMarkValue;
    private String passFlag;
    private Integer percentRangeHigh;
    private Integer percentRangeLow;
    private Date expiryDate;
    private Date effectiveDate;

}
