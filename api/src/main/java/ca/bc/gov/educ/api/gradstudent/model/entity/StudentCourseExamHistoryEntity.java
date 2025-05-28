package ca.bc.gov.educ.api.gradstudent.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false, exclude = "studentCourse")
@Entity
@Table(name = "STUDENT_COURSE_EXAM_HISTORY")
public class StudentCourseExamHistoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "STUDENT_COURSE_EXAM_HISTORY_ID", nullable = false)
    private UUID examHistoryID;

    @Column(name = "STUDENT_COURSE_EXAM_ID", nullable = false)
    private UUID studentCourseExamID;

    @Column(name = "STUDENT_COURSE_ID", nullable = false)
    private UUID studentCourseID;

    @Column(name = "SCHOOL_PERCENT")
    private Double schoolPercentage;

    @Column(name = "SCHOOL_BEST_PERCENT")
    private Double bestSchoolPercentage;

    @Column(name = "EXAM_PERCENT")
    private Double examPercentage;

    @Column(name = "EXAM_BEST_PERCENT")
    private Double bestExamPercentage;

    @Column(name = "EXAM_SPECIAL_CASE_CODE")
    private String specialCase;

    @Column(name = "TO_WRITE_FLAG")
    private String toWriteFlag;

    @Column(name = "WROTE_FLAG")
    private String wroteFlag;

    @OneToOne
    @JoinColumn(name = "STUDENT_COURSE_HISTORY_ID", referencedColumnName = "STUDENT_COURSE_HISTORY_ID", nullable = false)
    @JsonBackReference
    private StudentCourseHistoryEntity studentCourse;
}
