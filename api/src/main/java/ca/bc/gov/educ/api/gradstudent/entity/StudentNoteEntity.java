package ca.bc.gov.educ.api.gradstudent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "STUDENT_NOTE")
public class StudentNoteEntity extends BaseEntity {
   	
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
	
	@Column(name = "NOTE", nullable = true)
    private String note;
	
	@Column(name = "FK_GRAD_STUDENT_STUDENT_ID", nullable = false)
    private UUID studentID;
		
}