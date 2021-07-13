package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GradAlgorithmRules extends BaseModel {

	private UUID id; 
	private String ruleName; 
	private String ruleImplementation;
	private String ruleDescription;
	private Integer sortOrder;
	private String programCode;
	private String isActive;
	
}
