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

    @Column(name = "INTERIM_PERCENT")
    private Double interimPercent;

    @Column(name = "INTERIM_LETTER_GRADE")
    private String interimLetterGrade;

    @Column(name = "FINAL_PERCENT")
    private Double finalPercent;

    @Column(name = "FINAL_LETTER_GRADE")
    private String finalLetterGrade;

    @Column(name = "NUMBER_CREDITS", nullable = false)
    private Integer credits;

    @Column(name = "EQUIVALENT_OR_CHALLENGE_CODE")
    private String equivOrChallenge;

    @Column(name = "FINE_ARTS_APPLIED_SKILLS_CODE")
    private String fineArtsAppliedSkillsCode;

    @Column(name = "RELATED_COURSE_ID")
    private BigInteger relatedCourseId;

    @Column(name = "CUSTOM_COURSE_NAME")
    private String customCourseName;

    @Column(name = "STUDENT_COURSE_EXAM_ID")
    private UUID studentExamId;

}
