package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "EDW_GRADUATION_SNAPSHOT")
@IdClass(EdwSnapshotID.class)
public class EdwGraduationSnapshotEntity {
    @Id
    @Column(name = "GRAD_YEAR", nullable = false)
    private Long gradYear;
    @Id
    @Column(name = "STUD_NO", nullable = false)
    private String pen;
    @Column(name = "GRAD_FLAG", nullable = true)
    private String graduationFlag;
    @Column(name = "HONOUR_FLAG", nullable = true)
    private String honoursStanding;
    @Column(name = "STUD_GPA", nullable = true)
    private BigDecimal gpa;
    @Column(name = "GRAD_DATE", nullable = true)
    private LocalDate graduatedDate;

    @Column(name = "NOT_GRAD_REASON_1", nullable = true)
    private String nonGradReason1;
    @Column(name = "NOT_GRAD_REASON_2", nullable = true)
    private String nonGradReason2;
    @Column(name = "NOT_GRAD_REASON_3", nullable = true)
    private String nonGradReason3;
    @Column(name = "NOT_GRAD_REASON_4", nullable = true)
    private String nonGradReason4;
    @Column(name = "NOT_GRAD_REASON_5", nullable = true)
    private String nonGradReason5;
    @Column(name = "NOT_GRAD_REASON_6", nullable = true)
    private String nonGradReason6;
    @Column(name = "NOT_GRAD_REASON_7", nullable = true)
    private String nonGradReason7;
    @Column(name = "NOT_GRAD_REASON_8", nullable = true)
    private String nonGradReason8;
    @Column(name = "NOT_GRAD_REASON_9", nullable = true)
    private String nonGradReason9;
    @Column(name = "NOT_GRAD_REASON_10", nullable = true)
    private String nonGradReason10;
    @Column(name = "NOT_GRAD_REASON_11", nullable = true)
    private String nonGradReason11;
    @Column(name = "NOT_GRAD_REASON_12", nullable = true)
    private String nonGradReason12;

    @Column(name = "RUN_DATE", nullable = true)
    private LocalDate runDate;
    @Column(name = "SESSION_DATE", nullable = true)
    private LocalDate sessionDate;
    @Column(name = "PRINT_FRENCH_DOG", nullable = true)
    private String frenchDogwood;
    @Column(name = "PRINT_FRANCOPHONE", nullable = true)
    private String francophoneDogwood;

}
