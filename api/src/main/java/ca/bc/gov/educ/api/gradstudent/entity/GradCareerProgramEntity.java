package ca.bc.gov.educ.api.gradstudent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "CARRIER_PROGRAM_CODE")
public class GradCareerProgramEntity extends BaseEntity {
   
	@Id
	@Column(name = "CAREER_PROGRAM_CODE", nullable = false)
    private String careerProgramCode;

	@Column(name = "LABEL", nullable = false)
	private String label;

	@Column(name = "DESCRIPTION", nullable = false)
    private String careerProgramDescription;

	@Column(name = "EXPIRY_DATE", nullable = false)
	private Date expiryDate;

	@Column(name = "EFFECTIVE_DATE", nullable = false)
	private Date effectiveDate;

}