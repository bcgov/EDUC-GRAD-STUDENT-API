package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "GRADUATION_STUDENT_RECORD")
public class GraduationStudentRecordPaginationEntity extends BaseEntity {

	@Id
    @Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;

    @Column(name = "PEN")
    private String pen;

    @Column(name = "LEGAL_FIRST_NAME")
    private String legalFirstName;

    @Column(name = "LEGAL_MIDDLE_NAMES")
    private String legalMiddleNames;

    @Column(name = "LEGAL_LAST_NAME")
    private String legalLastName;

    @Column(name = "DOB")
    @Temporal(TemporalType.DATE)
    private Date dob;

    @Column(name = "GENDER_CODE")
    private String genderCode;

    @Column(name = "GRADUATION_PROGRAM_CODE")
    private String program;

    @Column(name = "GPA")
    private String gpa;

    @Column(name = "STUDENT_STATUS_CODE", nullable = false)
    private String studentStatus;

    @Column(name = "HONOURS_STANDING")
    private String honoursStanding;

    @Column(name = "PROGRAM_COMPLETION_DATE")
    @Temporal(TemporalType.DATE)
    private Date programCompletionDate;

    @Column(name = "RECALCULATE_GRAD_STATUS")
    private String recalculateGradStatus;

    @Column(name = "SCHOOL_OF_RECORD")
    private String schoolOfRecord;

    @Column(name = "STUDENT_GRADE")
    private String studentGrade;

    @Column(name = "STUDENT_GRAD_DATA")
    private String studentGradData;

    @Column(name = "SCHOOL_AT_GRADUATION")
    private String schoolAtGraduation;

    @Column(name = "RECALCULATE_PROJECTED_GRAD")
    private String recalculateProjectedGrad;

    @Column(name = "BATCH_ID")
    private Long batchId;

    @Column(name = "CONSUMER_EDUC_REQT_MET")
    private String consumerEducReqtMet;

    @Column(name = "STUDENT_PROJECTED_GRAD_DATA")
    private String studentProjectedGradData;

    @Column(name = "STUDENT_CITIZENSHIP_CODE")
    private String studentCitizenshipCode;

    @Column(name = "ADULT_START_DATE")
    @Temporal(TemporalType.DATE)
    private Date adultStartDate;

    @Column(name = "SCHOOL_OF_RECORD_ID")
    private UUID schoolOfRecordId;

    @Column(name = "SCHOOL_AT_GRADUATION_ID")
    private UUID schoolAtGraduationId;

}
