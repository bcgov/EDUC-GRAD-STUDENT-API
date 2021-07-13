package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
public class GradStudentCertificates extends BaseModel {

	private UUID id;	
	private String pen;	
	private String certificate;
	private String gradCertificateTypeCode;
	private String gradCertificateTypeDesc;
	private UUID studentID;
}
