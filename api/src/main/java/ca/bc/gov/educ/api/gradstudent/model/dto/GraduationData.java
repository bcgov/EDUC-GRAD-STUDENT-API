package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@JsonIgnoreProperties
public class GraduationData {
    private GradSearchStudent gradStudent;
    private School school;
    private String gradMessage;
    private boolean dualDogwood;
    private boolean isGraduated;
}
