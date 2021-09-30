package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GradStudentUngradReasons extends BaseModel {

	private UUID id;
	private String pen;
	private String ungradReasonCode;
	private String ungradReasonName;
	private UUID studentID;
	private String ungradReasonDescription;
}
