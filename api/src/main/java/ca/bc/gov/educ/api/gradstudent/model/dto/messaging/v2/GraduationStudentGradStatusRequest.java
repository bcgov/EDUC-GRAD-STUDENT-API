package ca.bc.gov.educ.api.gradstudent.model.dto.messaging.v2;

import ca.bc.gov.educ.api.gradstudent.util.GradLocalDateDeserializer;
import ca.bc.gov.educ.api.gradstudent.util.GradLocalDateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GraduationStudentGradStatusRequest {

    private UUID studentID;

    @JsonSerialize(using = GradLocalDateSerializer.class)
    @JsonDeserialize(using = GradLocalDateDeserializer.class)
    private LocalDate date;
}
