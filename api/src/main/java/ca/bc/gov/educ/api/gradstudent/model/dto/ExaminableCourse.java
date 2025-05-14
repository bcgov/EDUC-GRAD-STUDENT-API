package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.*;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ExaminableCourse {

    private String courseID;
    private Date examinableStart;
    private Date examinableEnd;
    private Date optionalStart;
    private Date optionalEnd;

}
