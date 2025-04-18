package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Immutable
@Table(name = "GRADUATION_STUDENT_RECORD")
public class GraduationStudentRecordSearchEntity {

	public GraduationStudentRecordSearchEntity() {
	}

    @Column(name = "GRADUATION_PROGRAM_CODE", nullable = true)
    private String program;
    
    @Column(name = "PROGRAM_COMPLETION_DATE", nullable = true)
    private Date programCompletionDate;

    @Column(name = "SCHOOL_OF_RECORD_ID", nullable = true)
    private String schoolOfRecordId;

    @Column(name = "SCHOOL_AT_GRADUATION_ID", nullable = true)
    private String schoolAtGradId;
    
    @Column(name = "STUDENT_STATUS_CODE", nullable = false)
    private String studentStatus;
    
    @Id
    @Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;
}