package ca.bc.gov.educ.api.gradstudent.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "STUDENT_RECORD_NOTE")
public class StudentRecordNoteEntity extends BaseEntity {
   	
	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
		name = "UUID",
		strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "STUDENT_RECORD_NOTE_ID", nullable = false)
    private UUID id;
	
	@Column(name = "RECORD_NOTE", nullable = true)
    private String note;
	
	@Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;
		
}