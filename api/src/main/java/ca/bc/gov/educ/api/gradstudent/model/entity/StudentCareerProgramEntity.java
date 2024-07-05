package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "STUDENT_CAREER_PROGRAM")
public class StudentCareerProgramEntity extends BaseEntity {
   
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
			name = "UUID",
			strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "STUDENT_CAREER_PROGRAM_ID", nullable = false)
    private UUID id;
	
	@Column(name = "CAREER_PROGRAM_CODE", nullable = false)
    private String careerProgramCode;
	
	@Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;
	
}