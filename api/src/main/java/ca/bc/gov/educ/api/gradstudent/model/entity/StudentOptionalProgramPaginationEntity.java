package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "STUDENT_OPTIONAL_PROGRAM")
public class StudentOptionalProgramPaginationEntity extends BaseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "STUDENT_OPTIONAL_PROGRAM_ID", nullable = false)
    private UUID studentOptionalProgramID;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false, targetEntity = GraduationStudentRecordPaginationEntity.class)
    @JoinColumn(name = "GRADUATION_STUDENT_RECORD_ID", referencedColumnName = "GRADUATION_STUDENT_RECORD_ID", updatable = false)
    GraduationStudentRecordPaginationEntity graduationStudentRecordEntity;

    @Column(name = "OPTIONAL_PROGRAM_ID", nullable = false)
    private UUID optionalProgramID;

    @Column(name = "COMPLETION_DATE")
    private Date completionDate;

}
