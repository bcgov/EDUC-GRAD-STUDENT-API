package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Builder
@AllArgsConstructor
@Table(name = "GRADUATION_STUDENT_RECORD")
public class GraduationStudentRecordEntity extends BaseEntity {

    public GraduationStudentRecordEntity(String gradProgram, UUID schoolOfRecordId) {
		this.program = gradProgram;
		this.schoolOfRecordId = schoolOfRecordId;
	}

	public GraduationStudentRecordEntity() {
	}

	@Transient
    private String pen;

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
    
    @Column(name = "STUDENT_GRADE", nullable = true)
    private String studentGrade;
    
    @Column(name = "STUDENT_STATUS_CODE", nullable = false)
    private String studentStatus;
    
    @Id
    @Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;

    @Column(name = "RECALCULATE_PROJECTED_GRAD", nullable = true)
    private String recalculateProjectedGrad;

    @Column(name = "BATCH_ID", nullable = true)
    private Long batchId;

    @Column(name = "CONSUMER_EDUC_REQT_MET", nullable = true)
    private String consumerEducationRequirementMet;

    @Column(name = "STUDENT_CITIZENSHIP_CODE", nullable = true)
    private String studentCitizenship;

    @Column(name = "ADULT_START_DATE", nullable = true)
    private Date adultStartDate;

    @Lob
    @Column(name = "STUDENT_PROJECTED_GRAD_DATA", columnDefinition="CLOB")
    private String studentProjectedGradData;

    @Column(name = "SCHOOL_OF_RECORD_ID", nullable = true)
    private UUID schoolOfRecordId;

    @Column(name = "SCHOOL_AT_GRADUATION_ID", nullable = true)
    private UUID schoolAtGradId;

    @Transient
    private String legalFirstName;

    @Transient
    private String legalMiddleNames;

    @Transient
    private String legalLastName;
}