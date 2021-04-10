package ca.bc.gov.educ.api.gradstudent.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import lombok.Data;

@Data
@Immutable
@Entity
@Table(name = "TAB_CNTRY")
public class GradCountryEntity {
   	
	@Id
	@Column(name = "CNTRY_CODE", nullable = false)
    private String countryCode; 
	
	@Column(name = "CNTRY_NAME", nullable = true)
    private String countryName; 
	
	@Column(name = "SRB_CNTRY_CODE", nullable = true)
    private String srbCountryCode; 	
		
}