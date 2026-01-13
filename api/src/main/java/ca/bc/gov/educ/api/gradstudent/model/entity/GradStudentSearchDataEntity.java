package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "GRADUATION_STUDENT_RECORD")
public class GradStudentSearchDataEntity extends BaseEntity {

    @Id
    @Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;

    @Column(name = "PEN", nullable = true)
    private String pen;

    @Column(name = "LEGAL_FIRST_NAME",  nullable = true)
    private String legalFirstName;

    @Column(name = "LEGAL_LAST_NAME",   nullable = true)
    private String legalLastName;

    @Column(name = "LEGAL_MIDDLE_NAMES", nullable = true)
    private String legalMiddleNames;

    @Column(name = "DOB",  nullable = true)
    private Date dob;

    @Column(name = "GENDER_CODE", nullable = true)
    private String genderCode;

    @Column(name = "STUDENT_GRADE", nullable = true)
    private String studentGrade;

    @Column(name = "GRADUATION_PROGRAM_CODE", nullable = true)
    private String program;
    
    @Column(name = "PROGRAM_COMPLETION_DATE", nullable = true)
    private Date programCompletionDate;

    @Column(name = "SCHOOL_OF_RECORD_ID", nullable = true)
    private UUID schoolOfRecordId;

    @Column(name = "SCHOOL_AT_GRADUATION_ID", nullable = true)
    private UUID schoolAtGraduationId;
    
    @Column(name = "STUDENT_STATUS_CODE", nullable = false)
    private String studentStatus;

    @Column(name = "ADULT_START_DATE", nullable = true)
    private Date adultStartDate;

    @Column(name = "RECALCULATE_GRAD_STATUS", nullable = true)
    private String recalculateGradStatus;

    @Column(name = "RECALCULATE_PROJECTED_GRAD", nullable = true)
    private String recalculateProjectedGrad;

}