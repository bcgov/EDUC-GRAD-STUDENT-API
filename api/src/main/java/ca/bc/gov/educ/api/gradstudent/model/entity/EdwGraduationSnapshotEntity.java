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

    @Column(name = "RUN_DATE", nullable = true)
    private LocalDate runDate;
    @Column(name = "SESSION_DATE", nullable = true)
    private LocalDate sessionDate;
    @Column(name = "SCHOOL_OF_RECORD", nullable = true)
    private String schoolOfRecord;

}
