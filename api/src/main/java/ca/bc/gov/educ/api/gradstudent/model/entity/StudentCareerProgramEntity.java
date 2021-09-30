package ca.bc.gov.educ.api.gradstudent.model.entity;

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
@Table(name = "STUDENT_CAREER_PROGRAM")
public class StudentCareerProgramEntity extends BaseEntity {
   
	@Id
	@Column(name = "STUDENT_CAREER_PROGRAM_ID", nullable = false)
    private UUID id;
	
	@Column(name = "CAREER_PROGRAM_CODE", nullable = false)
    private String careerProgramCode;
	
	@Column(name = "GRADUTION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;
	
}