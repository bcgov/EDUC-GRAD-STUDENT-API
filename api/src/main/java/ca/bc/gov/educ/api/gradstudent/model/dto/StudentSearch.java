package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

@Data
public class StudentSearch {

	private List<GradSearchStudent> gradSearchStudents;
	private Pageable pageable;
	private Integer totalPages;
	private Long totalElements;
	private Integer size;
	private Integer number;
	private Sort sort;
	private Integer numberOfElements;	
}
