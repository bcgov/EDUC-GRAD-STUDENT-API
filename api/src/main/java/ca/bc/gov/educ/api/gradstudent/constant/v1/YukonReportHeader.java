package ca.bc.gov.educ.api.gradstudent.constant.v1;

import lombok.Getter;

@Getter
public enum YukonReportHeader {

    SCHOOL_MINCODE_AT_GRAD("School Code at Graduation"),
    SCHOOL_NAME_AT_GRAD("School Name at Graduation"),
    PEN("PEN"),
    SURNAME("Surname"),
    FIRST_NAME("First Name"),
    MIDDLE_NAME("Middle Name"),
    GRAD_PROGRAM("Graduation Program"),
    OPTIONAL_PROGRAM("Optional Programs"),
    PROGRAM_COMPLETION_DATE("Completion Date")
    ;

    private final String code;
    YukonReportHeader(String code) { this.code = code; }
}
