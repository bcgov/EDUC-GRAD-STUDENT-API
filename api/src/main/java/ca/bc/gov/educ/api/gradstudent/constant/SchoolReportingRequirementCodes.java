package ca.bc.gov.educ.api.gradstudent.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public enum SchoolReportingRequirementCodes {

    CSF("CSF");
    @Getter
    private final String code;

    SchoolReportingRequirementCodes(String code) {
        this.code = code;
    }

    private static final SchoolReportingRequirementCodes[] ENUM_VALUES = values();

    public static Optional<SchoolReportingRequirementCodes> findByCode(String code) {
        return Arrays.stream(ENUM_VALUES)
            .filter(e -> e.code.equals(code))
            .findFirst();
    }

}
