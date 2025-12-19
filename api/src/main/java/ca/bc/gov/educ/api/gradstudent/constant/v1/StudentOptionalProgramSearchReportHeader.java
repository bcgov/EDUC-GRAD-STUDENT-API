package ca.bc.gov.educ.api.gradstudent.constant.v1;

import lombok.Getter;

@Getter
public enum StudentOptionalProgramSearchReportHeader {

    PEN("PEN"),
    STUDENT_STATUS("Student Status"),
    SURNAME("Surname"),
    GIVEN_NAME("Given Name"),
    MIDDLE_NAME("Middle Name"),
    BIRTHDATE("Birthdate"),
    GRADE("Grade"),
    PROGRAM("Program"),
    COMPLETION_DATE("Completion Date"),
    SCHOOL_OF_RECORD_CODE("School of Record Code"),
    SCHOOL_OF_RECORD_NAME("School of Record Name"),
    SCHOOL_OF_GRADUATION_CODE("School of Graduation Code"),
    SCHOOL_OF_GRADUATION_NAME("School of Graduation Name"),
    OPTIONAL_PROGRAM("Optional Program"),
    OPTIONAL_PROGRAM_COMPLETION_DATE("Optional Program Completion Date")
    ;

    private final String code;
    StudentOptionalProgramSearchReportHeader(String code) { this.code = code; }
}

