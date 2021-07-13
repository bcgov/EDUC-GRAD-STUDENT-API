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
@Table(name = "STUDENT_OPTIONAL_PROGRAMS")
public class GradStudentSpecialProgramEntity extends BaseEntity {

	@Id
	@GeneratedValue(generator = "UUID")
	@GenericGenerator(
		name = "UUID",
		strategy = "org.hibernate.id.UUIDGenerator"
	)
    @Column(name = "STUDENT_OPTIONAL_PROGRAM_ID", nullable = false)
    private UUID specialProgramID;

    @Column(name = "OPTIONAL_PROGRAM_CODE", nullable = false)
    private String programCode;

    @Lob
    @Column(name = "PROGRAM_NOTE", columnDefinition="CLOB")
    private String studentSpecialProgramData;

    @Column(name = "COMPLETION_DATE", nullable = true)
    private Date specialProgramCompletionDate;  
    
    @Column(name = "GRADUTION_STUDENT_RECORD_ID", nullable = false)
    private UUID studentID;

    @Transient
    private String pen;

    @Transient
    private UUID id;

}