package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.*;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class ExaminableCourse {

    private UUID examinableCourseID;
    private String courseCode;
    private String courseLevel;
    private String courseTitle;
    private Integer schoolWeightPercent;
    private Integer examWeightPercent;
    private String examinableStart;
    private String examinableEnd;
    private String optionalStart;
    private String optionalEnd;

}
