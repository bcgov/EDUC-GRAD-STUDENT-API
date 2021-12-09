package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentSearchRequest {
    String legalFirstName;
    String legalLastName;
    String legalMiddleNames;
    String usualFirstName;
    String usualLastName;
    String usualMiddleNames;
    String gender;
    String mincode;
    String localID;
    String birthdateFrom;
    String birthdateTo;
    String schoolOfRecord;
    String gradProgram;
}
