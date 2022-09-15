package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
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

    @JsonFormat(pattern = "yyyy-MM-dd")
    Date gradDateFrom;
    @JsonFormat(pattern = "yyyy-MM-dd")
    Date gradDateTo;

    Boolean validateInput;

    public List<String> getSchoolOfRecords() {
        if(schoolOfRecords == null) {
            schoolOfRecords = new ArrayList<>();
        }
        return schoolOfRecords;
    }

    public List<String> getDistricts() {
        if(districts == null) {
            districts = new ArrayList<>();
        }
        return districts;
    }

    public List<String> getSchoolCategoryCodes() {
        if(schoolCategoryCodes == null) {
            schoolCategoryCodes = new ArrayList<>();
        }
        return schoolCategoryCodes;
    }

    public List<String> getPens() {
        if(pens == null) {
            pens = new ArrayList<>();
        }
        return pens;
    }

    public List<String> getPrograms() {
        if(programs == null) {
            programs = new ArrayList<>();
        }
        return programs;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }
}
