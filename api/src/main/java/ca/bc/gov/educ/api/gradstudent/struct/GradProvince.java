package ca.bc.gov.educ.api.gradstudent.struct;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GradProvince {

	private String provCode;	
	private String provName;	
	private String countryCode;
	
	@Override
	public String toString() {
		return "GradProvince [provCode=" + provCode + ", provName=" + provName + ", countryCode=" + countryCode + "]";
	}				
}
