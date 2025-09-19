package ca.bc.gov.educ.api.gradstudent.model.dto;

import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    UUID schoolId;
    String localID;
    String birthdateFrom;
    String birthdateTo;
    String gradProgram;

    private List<UUID> schoolIds;
    private List<UUID> districtIds;
    private List<String> schoolCategoryCodes;
    private List<String> pens;
    private List<String> programs;
    private List<UUID> studentIDs;
    private List<String> statuses;
    private List<String> reportTypes;
    private List<String> grades;

    @JsonFormat(pattern= EducGradStudentApiConstants.DEFAULT_DATE_FORMAT)
    LocalDate gradDateFrom;
    @JsonFormat(pattern= EducGradStudentApiConstants.DEFAULT_DATE_FORMAT)
    LocalDate gradDateTo;

    Boolean validateInput;
    String activityCode;

    public List<UUID> getSchoolIds() {
        if(schoolIds == null) {
            schoolIds = new ArrayList<>();
        }
        return schoolIds;
    }

    public List<UUID> getDistrictIds() {
        if(districtIds == null) {
            districtIds = new ArrayList<>();
        }
        return districtIds;
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

    public List<String> getGrades() {
        if(grades == null) {
            grades = new ArrayList<>();
        }
        return grades;
    }

    public String toJson() {
        return new Gson().toJson(this);
    }

    @JsonIgnore
    public boolean isEmpty() {
        return  (schoolIds == null || schoolIds.isEmpty()) &&
                (districtIds == null || districtIds.isEmpty()) &&
                (schoolCategoryCodes == null || schoolCategoryCodes.isEmpty()) &&
                (pens == null || pens.isEmpty()) &&
                (studentIDs == null || studentIDs.isEmpty());
    }
}
