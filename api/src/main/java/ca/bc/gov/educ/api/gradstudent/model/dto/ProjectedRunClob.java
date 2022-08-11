package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
public class ProjectedRunClob {
    private List<GradRequirement> nonGradReasons;
    private boolean graduated;
}
