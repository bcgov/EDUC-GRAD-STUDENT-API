package ca.bc.gov.educ.api.gradstudent.struct;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type for Grad Student.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GradStudent {
    private String pen;
    /*private String archiveFlag;*/
    private String studSurname;
    private String studGiven;
    private String studMiddle;
    private String address1;
    private String address2;
    private String city;
    private String provinceCode;
    private String countryCode;
    private String postalCode;
    private String studBirth;
    private String studSex;
    private String mincode;
    /*private String studentCitizenship;
    private String studentGrade;
    private String studentLocalId;
    private String studTrueNo;
    private String studSin;
    private String programCode;
    private String programCode2;
    private String programCode3;
    private String studPsiPermit;
    private String studResearchPermit;
    private String studStatus;
    private String studConsedFlag;
    private String yrEnter11;
    private String gradDate;
    private String dogwoodFlag;
    private String honourFlag;
    private String mincode_grad;
    private String frenchDogwood;
    private String programCode4;
    private String programCode5;
    private String sccDate;
    private String gradRequirementYear;
    private String slpDate;
    private String mergedFromPen;
    private String gradReqtYearAtGrad;
    private String studGradeAtGrad;
    private String xcriptActvDate;
    private String allowedAdult;
    private String ssaNominationDate;
    private String adjTestYear;
    private String graduatedAdult;
    private String supplierNo;
    private String siteNo;
    private String emailAddress;
    private String englishCert;
    private String frenchCert;
    private String englishCertDate;
    private String frenchCertDate;
*/
    private String schoolName;
    private String countryName;
    private String provinceName;
    
	public String getStudSurname() {
		return studSurname != null ? studSurname.trim(): null;
	}
	public String getStudGiven() {
		return studGiven != null ? studGiven.trim(): null;
	}
	public String getStudMiddle() {
		return studMiddle != null ? studMiddle.trim(): null;
	}
	public String getAddress1() {
		return address1 != null ? address1.trim(): null;
	}
	public String getAddress2() {
		return address2 != null ? address2.trim(): null;
	}
	public String getPostalCode() {
		return postalCode != null ? postalCode.trim(): null;
	}
	public String getSchoolName() {
		return schoolName != null ? schoolName.trim(): null;
	}
	
	public String getCity() {
		return city != null ? city.trim(): null;
	}
    
}
