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
@Table(name = "STUDENT_OPTIONAL_PROGRAM")
public class GradStudentSpecialProgramEntity extends BaseEntity {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
		name = "UUID",
		strategy = "org.hibernate.id.UUIDGenerator"
	)
	@Column(name = "STUDENT_OPTIONAL_PROGRAM_ID", nullable = false)
    private UUID id;

    @Column(name = "STUDENT_OPTIONAL_PROGRAM_ID", nullable = false)
    private UUID specialProgramID;

    @Lob
    @Column(name = "PROGRAM_NOTE", columnDefinition="CLOB")
    private String studentSpecialProgramData;

    @Column(name = "COMPLETION_DATE", nullable = true)
    private Date specialProgramCompletionDate;  
    
    @Column(name = "GRADUTION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;

    @Column(name = "OPTIONAL_PROGRAM_CODE")
    private String optionalProgramCode;

}