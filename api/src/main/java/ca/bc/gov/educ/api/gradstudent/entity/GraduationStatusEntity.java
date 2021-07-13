package ca.bc.gov.educ.api.gradstudent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "GRADUATION_STUDENT_RECORD")
public class GraduationStatusEntity extends BaseEntity {

    @Lob
    @Column(name = "STUDENT_GRAD_DATA", columnDefinition="CLOB")
    private String studentGradData;

    @Column(name = "GRADUATION_PROGRAM_CODE", nullable = true)
    private String program;
    
    @Column(name = "PROGRAM_COMPLETION_DATE", nullable = true)
    private Date programCompletionDate; 
    
    @Column(name = "GPA", nullable = true)
    private String gpa;
    
    @Column(name = "HONOURS_STANDING", nullable = true)
    private String honoursStanding;        
    
    @Column(name = "RECALCULATE_GRAD_STATUS", nullable = true)
    private String recalculateGradStatus;
    
    @Column(name = "SCHOOL_OF_RECORD", nullable = true)
    private String schoolOfRecord;
    
    @Column(name = "STUDENT_GRADE", nullable = true)
    private String studentGrade;
    
    @Column(name = "GRAD_STUDENT_STATUS_CODE", nullable = false)
    private String studentStatus;
    
    @Id
    @Column(name = "GRADUTION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;
    
    @Column(name = "SCHOOL_AT_GRADUATION", nullable = true)
    private String schoolAtGrad;
    
    
    
}