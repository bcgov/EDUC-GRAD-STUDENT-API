package ca.bc.gov.educ.api.gradstudent.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public enum ProgramCodes {

    PF2023("2023-PF"),

    EN2023("2023-EN");

    @Getter
    private final String code;

    ProgramCodes(String code) {
        this.code = code;
    }

    private static final ProgramCodes[] ENUM_VALUES = values();

    public static Optional<ProgramCodes> findByCode(String code) {
        return Arrays.stream(ENUM_VALUES)
            .filter(e -> e.code.equals(code))
            .findFirst();
    }

}
