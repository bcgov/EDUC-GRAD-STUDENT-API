package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCoursePagination extends BaseModel {

    private UUID studentCourseID;
    @NotBlank
    private String courseID;
    @NotBlank
    private String courseSession;
    private Integer finalPercent;
    @NotNull(message = "credits cannot be null")
    private Integer credits;
    private String equivOrChallenge;
    private UUID studentExamId;

    private GraduationStudentPaginationRecord gradStudent;

}
