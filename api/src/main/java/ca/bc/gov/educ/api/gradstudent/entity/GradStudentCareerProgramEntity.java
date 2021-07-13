package ca.bc.gov.educ.api.gradstudent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "GRAD_STUDENT_CAREER_PROGRAM")
public class GradStudentCareerProgramEntity extends BaseEntity {
   
	@Id
	@Column(name = "ID", nullable = false)
    private UUID id; 
	
	@Column(name = "FK_GRAD_STUDENT_PEN", nullable = false)
    private String pen;
	
	@Column(name = "FK_GRAD_CAREER_PROGRAM_CODE", nullable = false)
    private String careerProgramCode;
	
	@Column(name = "FK_GRAD_STUDENT_STUDENT_ID", nullable = false)
    private UUID studentID;
	
}