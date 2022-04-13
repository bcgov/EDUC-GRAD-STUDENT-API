package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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

    List<String> schoolOfRecords;
    List<String> districts;
    List<String> schoolCategoryCodes;
    List<String> pens;
    List<String> programs;

    Boolean validateInput;

    public String toJson() {
        return new Gson().toJson(this);
    }
}
