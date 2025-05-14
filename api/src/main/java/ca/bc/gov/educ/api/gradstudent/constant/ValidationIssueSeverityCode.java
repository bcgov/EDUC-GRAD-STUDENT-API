package ca.bc.gov.educ.api.gradstudent.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public enum ValidationIssueSeverityCode {

    ERROR("ERROR","Error"),

    WARNING("WARNING","Warning");

    /**
     * The Code.
     */
    @Getter
    private final String label;
    @Getter
    private final String code;
    /**
     * Instantiates a new Pen request batch student validation field code.
     *
     * @param code the code
     */
    ValidationIssueSeverityCode(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static Optional<ValidationIssueSeverityCode> findByValue(String value) {
        return Arrays.stream(values()).filter(e -> Arrays.asList(e.code).contains(value)).findFirst();
    }
}
