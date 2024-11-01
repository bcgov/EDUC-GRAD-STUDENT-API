package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "STUDENT_GRADE_CODE")
public class StudentGradeCodeEntity extends BaseEntity {
   
	@Id
	@Column(name = "STUDENT_GRADE_CODE", unique = true, updatable = false)
	private String studentGradeCode;

	@NotNull(message = "label cannot be null")
	@Column(name = "LABEL")
	private String label;

	@NotNull(message = "displayOrder cannot be null")
	@Column(name = "DISPLAY_ORDER")
	private Integer displayOrder;

	@NotNull(message = "description cannot be null")
	@Column(name = "DESCRIPTION")
	private String description;

	@NotNull(message = "effectiveDate cannot be null")
	@Column(name = "EFFECTIVE_DATE")
	private LocalDateTime effectiveDate;
	
	@Column(name = "EXPIRY_DATE")
	private LocalDateTime expiryDate;

	@NotNull(message = "expected cannot be null")
	@Column(name = "EXPECTED")
	private String expected;
}