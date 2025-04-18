package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ReportGradStudentData implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID graduationStudentRecordId;
    private UUID schoolOfRecordId;
    private UUID schoolAtGradId;
    private UUID districtId;
    private String pen;
    private String firstName;
    private String middleName;
    private String lastName;
    private String studentGrade;
    private String studentStatus;
    private String districtName;
    private String schoolName;
    private String schoolAddress1;
    private String schoolAddress2;
    private String schoolCity;
    private String schoolProvince;
    private String schoolCountry;
    private String schoolPostal;
    private String programCode;
    private String programName;
    private String programCompletionDate;
    private String honorsStanding;
    private String graduated;
    private String transcriptTypeCode;
    private String localID;
    private String dob;
    private LocalDateTime updateDate;
    private List<CertificateType> certificateTypes;
    private List<NonGradReason> nonGradReasons;

}
