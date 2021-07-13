package ca.bc.gov.educ.api.gradstudent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.sql.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "GRAD_STUDENT_SPECIAL_PROGRAMS")
public class GradStudentSpecialProgramEntity extends BaseEntity {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
		name = "UUID",
		strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "ID", nullable = false)
    private UUID id; 
	
    @Column(name = "FK_GRAD_STUDENT_PEN", nullable = false)
    private String pen;
    
    @Column(name = "FK_GRAD_SPECIAL_PROGRAM_ID", nullable = false)
    private UUID specialProgramID;

    @Lob
    @Column(name = "STUDENT_SPECIAL_PROGRAM_DATA", columnDefinition="CLOB")
    private String studentSpecialProgramData;

    @Column(name = "SPECIAL_PROGRAM_COMP_DT", nullable = true)
    private Date specialProgramCompletionDate;  
    
    @Column(name = "FK_GRAD_STUDENT_STUDENT_ID", nullable = false)
    private UUID studentID;

}