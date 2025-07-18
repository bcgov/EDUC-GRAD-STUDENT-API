package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "GRADUATION_STUDENT_RECORD")
public class GraduationStudentRecordPaginationEntity extends BaseEntity {

	@Transient
    private String pen;

    @Column(name = "STUDENT_GRADE", nullable = true)
    private String studentGrade;
    
    @Column(name = "STUDENT_STATUS_CODE", nullable = false)
    private String studentStatus;
    
    @Id
    @Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;

    @Column(name = "SCHOOL_OF_RECORD_ID", nullable = true)
    private UUID schoolOfRecordId;

    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @OneToMany(mappedBy = "graduationStudentRecordEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL, targetEntity = StudentCoursePaginationEntity.class)
    Set<StudentCoursePaginationEntity> studentCoursePaginationEntities;
}
