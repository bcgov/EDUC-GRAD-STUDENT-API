package ca.bc.gov.educ.api.gradstudent.constant.v1;

import lombok.Getter;

@Getter
public enum ReportGradStudentDataHeader {

    PEN("PEN"),
    LOCAL_ID("LOCAL_ID"),
    LAST_NAME("LAST_NAME"),
    FIRST_NAME("FIRST_NAME"),
    MIDDLE_NAME("MIDDLE_NAME"),
    BIRTH_DATE("BIRTH_DATE"),
    GRADE("GRADE"),
    PROGRAM("PROGRAM"),
    PROGRAM_COMPLETION_DATE("PROGRAM_COMPLETION_DATE"),
    SCHOOL_AT_GRADUATION("SCHOOL_AT_GRADUATION"),
    HONOURS_STANDING("HONOURS_STANDING"),
    ;

    private final String code;
    ReportGradStudentDataHeader(String code) { this.code = code; }
}
