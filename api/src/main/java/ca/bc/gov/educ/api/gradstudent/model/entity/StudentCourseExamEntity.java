package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@Entity
@EqualsAndHashCode(callSuper = false)
@Table(name = "STUDENT_COURSE_EXAM")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCourseExamEntity extends BaseEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "STUDENT_COURSE_EXAM_ID", nullable = false)
    private UUID id;

    @Column(name = "SCHOOL_PERCENT")
    private Integer schoolPercentage;

    @Column(name = "SCHOOL_BEST_PERCENT")
    private Integer bestSchoolPercentage;

    @Column(name = "EXAM_PERCENT")
    private Integer examPercentage;

    @Column(name = "EXAM_BEST_PERCENT")
    private Integer bestExamPercentage;

    @Column(name = "EXAM_SPECIAL_CASE_CODE")
    private String specialCase;

    @Column(name = "TO_WRITE_FLAG")
    private String toWriteFlag;

    @Column(name = "WROTE_FLAG")
    private String wroteFlag;

}
