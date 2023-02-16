package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Table(name = "REPORT_GRAD_STUDENT_DATA_VW")
public class ReportGradStudentDataEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID graduationStudentRecordId;

    @Column(name = "MINCODE")
    private String mincode;

    @Column(name = "PEN")
    private String pen;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "MIDDLE_NAME")
    private String middleName;

    @Column(name = "LAST_NAME")
    private String lastName;

    @Column(name = "DISTRICT_NAME")
    private String districtName;

    @Column(name = "SCHOOL_NAME")
    private String schoolName;

    @Column(name = "PROGRAM_CODE")
    private String programCode;

    @Column(name = "PROGRAM_NAME")
    private String programName;

    @Column(name = "PROGRAM_COMPLETION_DATE")
    private String programCompletionDate;

    @Column(name = "IS_GRADUATED")
    private String graduated;

    @Column(name = "TRANSCRIPT_TYPE_CODE")
    private String transcriptTypeCode;

    @Column(name = "CERTIFICATE_TYPE_CODES")
    private String certificateTypeCodes;

}
