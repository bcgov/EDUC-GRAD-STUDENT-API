package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GradStudentSpecialProgram extends BaseModel{

	private UUID id;
    private String pen;
    private UUID specialProgramID;
    private String studentSpecialProgramData;
    private String specialProgramCompletionDate;
    private String specialProgramName;
    private String specialProgramCode;
    private String programCode;
    private UUID studentID;
				
}
