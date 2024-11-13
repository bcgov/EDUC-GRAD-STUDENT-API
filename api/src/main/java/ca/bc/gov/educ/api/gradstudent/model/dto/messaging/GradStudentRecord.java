package ca.bc.gov.educ.api.gradstudent.model.dto.messaging;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GradStudentRecord {

    private UUID studentID;
    private Date dob;

}
