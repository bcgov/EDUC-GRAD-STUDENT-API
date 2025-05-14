package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@JsonIgnoreProperties
public class GraduationDataOptionalDetails extends GraduationData {
    private GraduationStudentRecord gradStatus;
    private List<GradStudentOptionalStudentProgram> optionalGradStatus;
}
