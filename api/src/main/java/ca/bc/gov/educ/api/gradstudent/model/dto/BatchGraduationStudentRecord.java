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
	private UUID schoolOfRecordId;

	public BatchGraduationStudentRecord(String program, Date programCompletionDate, UUID schoolOfRecordId, UUID studentID) {
		this.program = program;
		this.programCompletionDate = EducGradStudentApiUtils.formatDate(programCompletionDate, PROGRAM_COMPLETION_DATE_FORMAT);
		this.schoolOfRecordId = schoolOfRecordId;
		this.studentID = studentID;
	}
}
