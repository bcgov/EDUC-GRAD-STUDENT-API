package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Data
@Component
public class StudentList {

	List<UUID> studentids;
}
