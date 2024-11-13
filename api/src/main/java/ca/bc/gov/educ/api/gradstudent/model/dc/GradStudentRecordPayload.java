package ca.bc.gov.educ.api.gradstudent.model.dc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradStudentRecordPayload {

    private String dob;
    private String exception;

}
