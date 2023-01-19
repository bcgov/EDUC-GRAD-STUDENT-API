package ca.bc.gov.educ.api.gradstudent.model.dto;

import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.PROGRAM_COMPLETION_DATE_FORMAT;

@Data
public class BatchGraduationStudentRecord {

	private UUID studentID;
	private String program;
	private String programCompletionDate;
	private String schoolOfRecord;

	public BatchGraduationStudentRecord(String program, Date programCompletionDate, String schoolOfRecord, UUID studentID) {
		this.program = program;
		this.programCompletionDate = EducGradStudentApiUtils.formatDate(programCompletionDate, PROGRAM_COMPLETION_DATE_FORMAT);
		this.schoolOfRecord = schoolOfRecord;
		this.studentID = studentID;
	}
}
