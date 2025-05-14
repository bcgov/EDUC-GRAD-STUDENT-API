package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "STUDENT_COURSE_EXAM")
public class StudentCourseExamEntity extends BaseEntity {

    @Id
    @Column(name = "STUDENT_COURSE_EXAM_ID", nullable = false)
    private UUID id;

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
    
}
