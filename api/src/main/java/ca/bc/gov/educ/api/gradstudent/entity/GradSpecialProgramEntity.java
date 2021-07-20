package ca.bc.gov.educ.api.gradstudent.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "OPTIONAL_PROGRAM_CODE")
public class GradSpecialProgramEntity extends BaseEntity {

	@Id
	@Column(name = "OPTIONAL_PROGRAM_CODE", nullable = false)
    private String specialProgramCode;

	@Column(name = "OPTIONAL_PROGRAM_NAME", nullable = false)
    private String specialProgramDescription;

}