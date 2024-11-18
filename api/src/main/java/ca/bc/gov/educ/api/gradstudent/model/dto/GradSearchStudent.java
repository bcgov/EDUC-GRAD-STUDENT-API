package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.sql.Date;

import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.DEFAULT_DATE_FORMAT;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class GradSearchStudent {

	private String studentID;
	private String pen;
	private String legalFirstName;
	private String legalMiddleNames;
	private String legalLastName;
	private String dob;
	private String sexCode;
	private String genderCode;
	private String studentCitizenship;
	private String usualFirstName;
	private String usualMiddleNames;
	private String usualLastName;
	private String email;
	private String emailVerified;
	private String deceasedDate;
	private String postalCode;
	private String mincode;
	private String localID;
	private String gradeCode;
	private String gradeYear;
	private String demogCode;
	private String statusCode;
	private String memo;
	private String trueStudentID;
	private String program;
	private String schoolOfRecord;
	private String schoolOfRecordId;
	private String schoolOfRecordName;
	private String schoolOfRecordindependentAffiliation;
	private String studentGrade;
	private String studentStatus;
	private String transcriptEligibility;
	private String certificateEligibility;
	@JsonFormat(pattern=DEFAULT_DATE_FORMAT)
	private Date adultStartDate;

}
