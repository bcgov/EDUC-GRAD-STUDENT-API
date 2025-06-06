package ca.bc.gov.educ.api.gradstudent.model.dto.external.gdc.v1;

import ca.bc.gov.educ.api.gradstudent.model.dto.BaseModel;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class DemographicStudent extends BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private String mincode;

    private String schoolID;

    private String schoolReportingRequirementCode;

    private String birthdate;

    private String pen;

    private String citizenship;

    private String grade;

    private String programCode1;

    private String programCode2;

    private String programCode3;

    private String programCode4;

    private String programCode5;

    private String gradRequirementYear;

    private String schoolCertificateCompletionDate;

    private String studentStatus;

    private String isSummerCollection;

    private String vendorID;

}
