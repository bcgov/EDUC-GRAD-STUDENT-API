package ca.bc.gov.educ.api.gradstudent.model.dc;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GradStudentRecordPayload {

    private String studentID;
    private String exception;
    private String program;
    private String programCompletionDate;
    private String schoolOfRecordId;
    private String studentStatusCode;
    private String graduated;
    private String schoolAtGradId;

}
