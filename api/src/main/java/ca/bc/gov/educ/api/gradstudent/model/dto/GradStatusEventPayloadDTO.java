package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradStatusEventPayloadDTO {
    private String pen;
    private String program;
    private String programCompletionDate;
    private UUID schoolOfRecordId;
    private UUID schoolAtGradId;
    private String studentGrade;
    private String studentStatus;
    private String honoursStanding;

    private String updateUser;
}
