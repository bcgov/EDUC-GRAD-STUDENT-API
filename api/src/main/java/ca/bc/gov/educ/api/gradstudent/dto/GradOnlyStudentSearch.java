package ca.bc.gov.educ.api.gradstudent.dto;

import java.util.List;

import lombok.Data;

@Data
public class GradOnlyStudentSearch {

	private List<GradSearchStudent> gradSearchStudents;
	private String searchMessage;
}
