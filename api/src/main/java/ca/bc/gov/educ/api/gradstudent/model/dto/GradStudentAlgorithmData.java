package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class GradStudentAlgorithmData {

	private GradSearchStudent gradStudent;
	private GraduationStudentRecord graduationStudentRecord;
	private List<StudentCareerProgram> studentCareerProgramList;
}
