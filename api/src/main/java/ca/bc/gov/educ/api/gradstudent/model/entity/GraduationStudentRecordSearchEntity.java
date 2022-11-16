package ca.bc.gov.educ.api.gradstudent.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
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
    
    @Column(name = "SCHOOL_OF_RECORD", nullable = true)
    private String schoolOfRecord;
    
    @Column(name = "STUDENT_STATUS_CODE", nullable = false)
    private String studentStatus;
    
    @Id
    @Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;
}