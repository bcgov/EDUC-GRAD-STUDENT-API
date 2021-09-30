package ca.bc.gov.educ.api.gradstudent.model.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "STUDENT_STATUS_CODE")
public class StudentStatusEntity extends BaseEntity {
   
	@Id
	@Column(name = "STUDENT_STATUS_CODE", nullable = false)
    private String code; 
	
	@Column(name = "LABEL", nullable = true)
    private String label; 
	
	@Column(name = "DISPLAY_ORDER", nullable = true)
    private int displayOrder; 
	
	@Column(name = "DESCRIPTION", nullable = true)
    private String description;
	
	@Column(name = "EFFECTIVE_DATE", nullable = true)
    private Date effectiveDate; 
	
	@Column(name = "EXPIRY_DATE", nullable = true)
    private Date expiryDate;
	
	
}