package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentCourseExam extends BaseModel {

    private Integer schoolPercentage;
    private Integer bestSchoolPercentage;
    private Integer bestExamPercentage;
    private String specialCase;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private UUID id;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer examPercentage;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String toWriteFlag;
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String wroteFlag;

}
