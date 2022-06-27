package ca.bc.gov.educ.api.gradstudent.model.dto;

import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiUtils;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
public class BatchGraduationStudentRecord {

	private String studentGradData;
	private String pen;
	private String program;
	private String programName;
	private String programCompletionDate;
	private String gpa;
	private String honoursStanding;
	private String recalculateGradStatus;
	private String schoolOfRecord;
	private String schoolName;
	private String studentGrade;
	private String studentStatus;
	private String studentStatusName;
	private UUID studentID;
	private String schoolAtGrad;
	private String schoolAtGradName;
	private String recalculateProjectedGrad;
	private Long batchId;
	private String consumerEducationRequirementMet;
	private String studentProjectedGradData;
	private String legalFirstName;
	private String legalMiddleNames;
	private String legalLastName;

	public BatchGraduationStudentRecord(String program, Date programCompletionDate, String schoolOfRecord, String studentGrade, String studentStatus, UUID studentID) {
		this.program = program;
		this.programCompletionDate = EducGradStudentApiUtils.parseTraxDate(programCompletionDate != null ? programCompletionDate.toString():null);
		this.schoolOfRecord = schoolOfRecord;
		this.studentGrade = studentGrade;
		this.studentStatus = studentStatus;
		this.studentID = studentID;
	}
}
