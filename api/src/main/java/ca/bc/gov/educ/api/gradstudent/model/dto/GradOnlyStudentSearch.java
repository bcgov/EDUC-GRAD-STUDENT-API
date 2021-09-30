package ca.bc.gov.educ.api.gradstudent.model.dto;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.Data;

@Data
public class GradOnlyStudentSearch {

	private List<GradSearchStudent> gradSearchStudents;
	private String searchMessage;
	private Pageable pageable;
	private Integer totalPages;
	private Long totalElements;
	private Integer size;
	private Integer number;
	private Sort sort;
	private Integer numberOfElements;
}
