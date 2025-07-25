package ca.bc.gov.educ.api.gradstudent.model.dto.external.program.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class GraduationProgramCode implements Serializable {
    private static final long serialVersionUID = 123456789022345678L;

    private String programCode;
    private String programName;
    private String description;
    private int displayOrder;
    private String effectiveDate;
    private String expiryDate;
    private String associatedCredential;
}
