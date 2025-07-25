package ca.bc.gov.educ.api.gradstudent.model.entity;

import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.*;
import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "STUDENT_OPTIONAL_PROGRAM")
public class StudentOptionalProgramEntity extends BaseEntity {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
		name = "UUID",
		strategy = "org.hibernate.id.UUIDGenerator"
	)
    @Column(name = "STUDENT_OPTIONAL_PROGRAM_ID", nullable = false)
    private UUID id;

    @Column(name = "OPTIONAL_PROGRAM_ID", nullable = false)
    private UUID optionalProgramID;
    
    @Lob
    @Column(name = "PROGRAM_NOTE", columnDefinition="CLOB")
    private String studentOptionalProgramData;

    @Column(name = "COMPLETION_DATE")
    private Date optionalProgramCompletionDate;
    
    @Column(name = "GRADUATION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;

}