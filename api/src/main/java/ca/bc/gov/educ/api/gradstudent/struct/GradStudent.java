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
    private String studSurname;
    private String studGiven;
    private String studMiddle;
    private String studBirth;
    private String studSex;
    private String address1;
    private String address2;
    private String city;
    private String postalCode;
    private String provinceCode;
    private String provinceName;
    private String countryCode;
    private String countryName;
    private String studentGrade;
    private String studentLocalId;
    private String studentCitizenship;
    private String mincode;
    private String studentStatus;
    private String archiveFlag;
    private int gradRequirementYear;
    private String programCode;
    private String programCode2;
    private String programCode3;
    private String programCode4;
    private String programCode5;
    private String programName;
}
