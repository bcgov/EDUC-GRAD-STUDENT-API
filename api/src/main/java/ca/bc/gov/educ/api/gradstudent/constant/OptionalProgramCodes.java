package ca.bc.gov.educ.api.gradstudent.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum OptionalProgramCodes {

    DD("DD"),   // English Dual Dogwood
    FR("FR"),   // French Language Certificate
    FI("FI");   // French Immersion

    @Getter
    private final String code;

    OptionalProgramCodes(String code) {
        this.code = code;
    }

    private static final OptionalProgramCodes[] ENUM_VALUES = values();

    public static Optional<OptionalProgramCodes> findByCode(String code) {
        return Arrays.stream(ENUM_VALUES)
            .filter(e -> e.code.equals(code))
            .findFirst();
    }

    public static List<String> getProtectedCodes() {
        return Arrays.asList(FI.getCode(), FR.getCode(), DD.getCode());
    }

}
