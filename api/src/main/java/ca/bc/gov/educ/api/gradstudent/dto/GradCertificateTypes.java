package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GradCertificateTypes extends BaseModel {

	private String code;	
	private String description;
	
	@Override
	public String toString() {
		return "GradCertificateTypes [code=" + code + ", description=" + description + "]";
	}
	
	
}
