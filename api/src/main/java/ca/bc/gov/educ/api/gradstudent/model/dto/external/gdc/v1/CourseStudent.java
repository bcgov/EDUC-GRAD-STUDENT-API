package ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1;

import ca.bc.gov.educ.api.gradstudent.model.dto.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.List;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class CourseStudent extends BaseModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private String  pen;

    private String submissionModeCode;

    private String isSummerCollection;

    List<CourseStudentDetail> studentDetails;

}
