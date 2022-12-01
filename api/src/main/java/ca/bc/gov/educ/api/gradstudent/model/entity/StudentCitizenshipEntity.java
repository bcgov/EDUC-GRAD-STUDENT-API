package ca.bc.gov.educ.api.gradstudent.model.entity;

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
@Table(name = "STUDENT_CITIZENSHIP_CODE")
public class StudentCitizenshipEntity extends BaseEntity {
   
	@Id
	@Column(name = "STUDENT_CITIZENSHIP_CODE", nullable = false)
    private String code; 
	
	@Column(name = "LABEL", nullable = false)
    private String label; 
	
	@Column(name = "DISPLAY_ORDER", nullable = false)
    private int displayOrder; 
	
	@Column(name = "DESCRIPTION", nullable = false)
    private String description;
	
	@Column(name = "EFFECTIVE_DATE", nullable = false)
    private Date effectiveDate; 
	
	@Column(name = "EXPIRY_DATE", nullable = true)
    private Date expiryDate;
	
	
}