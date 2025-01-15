package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@Component
public class SchoolClob {

	private String minCode;
	private String schoolId;
    private String schoolName;
	private String districtId;
    private String districtName;
    private String transcriptEligibility;    
    private String certificateEligibility;
    private String address1;
    private String address2;    
    private String city;    
    private String provCode; 
    private String countryCode;
    private String postal;
	private String openFlag;
	private String schoolCategoryCode;
	private String schoolCategoryLegacyCode;

	@Override
	public String toString() {
		return "SchoolClob [minCode=" + minCode + ", schoolId=" + schoolId + ", schoolName=" + schoolName + ", schoolCategoryCode=" + schoolCategoryCode + ", schoolCategoryLegacyCode=" + schoolCategoryLegacyCode
				+ ", districtId=" + districtId + ", districtName=" + districtName + ", transcriptEligibility=" + transcriptEligibility + ", certificateEligibility=" + certificateEligibility
				+ ", address1=" + address1 + ", address2=" + address2 + ", city=" + city + ", provCode=" + provCode + ", countryCode=" + countryCode + ", postal=" + postal + ", openFlag=" + openFlag
				+ "]";
	}

}
