package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StudentDemographic extends Student {

    String gradProgram;
    String gradDate;
    String dogwoodFlag;
    String frenchCert;
    String englishCert;
    String sccDate;
    String transcriptEligibility;
    String schoolCategory;
    String schoolType;
    String schoolName;
    String formerStudent;
    @JsonIgnore
    String createUser;
    @JsonIgnore
    String updateUser;
    @JsonIgnore
    String createDate;
    @JsonIgnore
    String updateDate;

}
