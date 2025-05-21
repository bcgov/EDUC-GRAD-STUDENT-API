package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.*;

import java.util.Date;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ExaminableCourse {

    private UUID examinableCourseID;
    private String courseID;
    private Date examinableStart;
    private Date examinableEnd;
    private Date optionalStart;
    private Date optionalEnd;

}
