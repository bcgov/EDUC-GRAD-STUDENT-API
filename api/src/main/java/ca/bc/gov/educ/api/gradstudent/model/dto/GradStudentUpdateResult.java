package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Component
@SuperBuilder
public class GradStudentUpdateResult {

    private boolean schoolOfRecordUpdated;
    private boolean citizenshipUpdated;

}
