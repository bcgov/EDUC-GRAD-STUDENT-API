package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;

import java.math.BigInteger;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "STUDENT_COURSE")
public class StudentCoursePaginationEntity extends BaseEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "STUDENT_COURSE_ID", nullable = false)
    private UUID studentCourseID;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(optional = false, targetEntity = GraduationStudentRecordPaginationEntity.class)
    @JoinColumn(name = "GRADUATION_STUDENT_RECORD_ID", referencedColumnName = "GRADUATION_STUDENT_RECORD_ID", updatable = false)
    GraduationStudentRecordPaginationEntity graduationStudentRecordEntity;

    @Column(name = "COURSE_ID", nullable = false)
    private BigInteger courseID;

    @Column(name = "COURSE_SESSION", nullable = false)
    private String courseSession;

    @Column(name = "FINAL_PERCENT")
    private Double completedCoursePercentage;

    @Column(name = "NUMBER_CREDITS", nullable = false)
    private Integer credits;

    @Column(name = "EQUIVALENT_OR_CHALLENGE_CODE")
    private String equivOrChallenge;

    @Column(name = "STUDENT_COURSE_EXAM_ID")
    private UUID studentExamId;

}
