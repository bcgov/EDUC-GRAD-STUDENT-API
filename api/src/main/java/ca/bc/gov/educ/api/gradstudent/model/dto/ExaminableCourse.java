package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ExaminableCourse {

    private String courseID;
    private Date examinableStart;
    private Date examinableEnd;
    private Date optionalStart;
    private Date optionalEnd;

}
