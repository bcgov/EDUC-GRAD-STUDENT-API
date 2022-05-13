package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
public class StudentDemographic extends Student {

    String gradProgram;
    String gradDate;
    String dogwoodFlag;
    String frenchCert;
    String englishCert;
    String sccDate;
    String transcriptEligibility;
    String schoolCategory;
    String schoolName;
    String formerStudent;

}
