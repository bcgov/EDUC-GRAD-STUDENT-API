package ca.bc.gov.educ.api.gradstudent.struct;

import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
public class GradCountry {

	private String countryCode; 	
	private String countryName;	
	private String srbCountryCode;
	
	@Override
	public String toString() {
		return "GradCountry [countryCode=" + countryCode + ", countryName=" + countryName + ", srbCountryCode="
				+ srbCountryCode + "]";
	}
	
}
