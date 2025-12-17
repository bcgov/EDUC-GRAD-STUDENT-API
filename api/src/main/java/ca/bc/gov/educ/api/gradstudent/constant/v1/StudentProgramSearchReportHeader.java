package ca.bc.gov.educ.api.gradstudent.constant.v1;

import lombok.Getter;

@Getter
public enum StudentProgramSearchReportHeader {

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
    ADULT_START_DATE("Adult Start Date"),
    RECALCULATE_GRAD_STATUS("Recalculate Grad Status?"),
    RECALCULATE_PROJECTED_GRAD("Recalculate Projected Grad?")
    ;

    private final String code;
    StudentProgramSearchReportHeader(String code) { this.code = code; }
}

