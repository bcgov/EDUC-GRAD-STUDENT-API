package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
public class GraduationStudentRecordSearchResult {

    public static final String PEN_VALIDATION_ERROR = "STUDENTs not exist in GRAD:";
    public static final String STUDENT_STATUS_VALIDATION_WARNING = "WARNING: STUDENTs with status %s:";
    public static final String STUDENT_STATUS_VALIDATION_ERROR = "ERROR: STUDENTs with status %s:";
    public static final String SCHOOL_VALIDATION_ERROR = "The following SCHOOLs not exist:";
    public static final String DISTRICT_VALIDATION_ERROR = "The following DISTRICTs not exist:";
    public static final String PROGRAM_VALIDATION_ERROR = "The following PROGRAMs not exist in GRAD:";

    private List<UUID> studentIDs;
    @JsonInclude(content = JsonInclude.Include.NON_EMPTY)
    private Map<String, List<String>> validationErrors = new HashMap<>();

    public void addError(String key, String value) {
        List<String> errors = validationErrors.get(key);
        if(errors == null) {
            errors = new ArrayList<>();
            validationErrors.put(key, errors);
        }
        errors.add(value);
    }

}
