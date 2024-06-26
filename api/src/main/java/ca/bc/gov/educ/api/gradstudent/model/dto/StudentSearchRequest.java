package ca.bc.gov.educ.api.gradstudent.model.dto;

import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.gson.Gson;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class StudentSearchRequest implements Serializable {
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

    private List<String> schoolOfRecords;
    private List<String> districts;
    private List<String> schoolCategoryCodes;
    private List<String> pens;
    private List<String> programs;
    private List<UUID> studentIDs;

    @JsonFormat(pattern= EducGradStudentApiConstants.DEFAULT_DATE_FORMAT)
    LocalDate gradDateFrom;
    @JsonFormat(pattern= EducGradStudentApiConstants.DEFAULT_DATE_FORMAT)
    LocalDate gradDateTo;

    Boolean validateInput;
    String activityCode;

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
