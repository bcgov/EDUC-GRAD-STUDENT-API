package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class StudentOptionalProgramHistory extends BaseModel{

    private UUID historyId;
    private String activityCode;
	private UUID studentOptionalProgramId;
    private String pen;
    private UUID optionalProgramID;
    private String studentOptionalProgramData;
    private String optionalProgramCompletionDate;
    private String optionalProgramName;
    private String optionalProgramCode;
    private String programCode;
    private UUID studentID;
				
}
