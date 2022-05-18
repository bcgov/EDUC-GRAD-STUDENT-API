package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProjectedRunClob {
    private List<GradRequirement> nonGradReasons;
    private List<GradRequirement> requirementsMet;
    private String gradMessage;
    private boolean dualDogwood;
    private boolean graduated;
}
