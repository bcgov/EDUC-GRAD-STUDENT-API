package ca.bc.gov.educ.api.gradstudent.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "GRADUATION_STUDENT_RECORD")
public class GraduationStudentRecordEntity extends BaseEntity {

    public GraduationStudentRecordEntity(String gradProgram, String schoolOfRecord) {
		this.program = gradProgram;
		this.schoolOfRecord = schoolOfRecord;
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
    
    @Column(name = "SCHOOL_OF_RECORD", nullable = true)
    private String schoolOfRecord;
    
    @Column(name = "STUDENT_GRADE", nullable = true)
    private String studentGrade;
    
    @Column(name = "STUDENT_STATUS_CODE", nullable = false)
    private String studentStatus;
    
    @Id
    @Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;
    
    @Column(name = "SCHOOL_AT_GRADUATION", nullable = true)
    private String schoolAtGrad;

    @Column(name = "RECALCULATE_PROJECTED_GRAD", nullable = true)
    private String recalculateProjectedGrad;

    @Column(name = "BATCH_ID", nullable = true)
    private Long batchId;

    @Column(name = "CONSUMER_EDUC_REQT_MET", nullable = true)
    private String consumerEducationRequirementMet;

    @Column(name = "STUDENT_CITIZENSHIP_CODE", nullable = true)
    private String studentCitizenship;

    @Lob
    @Column(name = "STUDENT_PROJECTED_GRAD_DATA", columnDefinition="CLOB")
    private String studentProjectedGradData;

    @Transient
    private String legalFirstName;

    @Transient
    private String legalMiddleNames;

    @Transient
    private String legalLastName;

    @OneToMany
    @JoinColumn(name = "GRADUATION_STUDENT_RECORD_ID", insertable = false, updatable = false)
    private List<StudentCareerProgramEntity> careerPrograms;
}