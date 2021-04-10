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
@Table(name = "TAB_SCHOOL")
public class SchoolEntity {
   
	@Id
	@Column(name = "MINCODE", nullable = true)
    private String minCode;   

    @Column(name = "SCHL_NAME", nullable = true)
    private String schoolName;  
    
    @Column(name = "XCRIPT_ELIG", nullable = true)
    private String transcriptEligibility;  
    
    @Column(name = "DOGWOOD_ELIG", nullable = true)
    private String certificateEligibility;  

    @Column(name = "SCHL_IND_TYPE", nullable = true)
    private String independentDesignation;
    
    @Column(name = "MAILER_TYPE", nullable = true)
    private String mailerType;
    
    @Column(name = "ADDRESS1", nullable = true)
    private String address1;
    
    @Column(name = "ADDRESS2", nullable = true)
    private String address2;
    
    @Column(name = "CITY", nullable = true)
    private String city;
    
    @Column(name = "PROV_CODE", nullable = true)
    private String provCode;
    
    @Column(name = "CNTRY_CODE", nullable = true)
    private String countryCode;
    
    @Column(name = "POSTAL", nullable = true)
    private String postal;

}
