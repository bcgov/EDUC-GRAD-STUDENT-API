package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class StudentCareerProgramRequestDTO extends BaseModel {

	private List<String> careerProgramCodes = new ArrayList<>();

}
