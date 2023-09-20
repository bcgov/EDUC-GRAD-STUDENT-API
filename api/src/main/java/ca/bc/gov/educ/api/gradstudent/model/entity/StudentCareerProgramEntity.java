package ca.bc.gov.educ.api.gradstudent.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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
	
	@Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;
	
}