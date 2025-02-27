package ca.bc.gov.educ.api.gradstudent.model.dto.messaging.v2;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class GraduationStudentGradStatusResponse {

    private UUID studentID;
    private Boolean isgraduated;
    private String exception;
}
