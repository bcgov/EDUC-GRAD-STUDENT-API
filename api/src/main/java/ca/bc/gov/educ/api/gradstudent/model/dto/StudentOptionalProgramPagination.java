package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentOptionalProgramPagination extends BaseModel {

    private UUID studentOptionalProgramID;

    @NotNull(message = "optionalProgramID cannot be null")
    private UUID optionalProgramID;

    private Date completionDate;

    private GraduationStudentPaginationRecord gradStudent;

}

