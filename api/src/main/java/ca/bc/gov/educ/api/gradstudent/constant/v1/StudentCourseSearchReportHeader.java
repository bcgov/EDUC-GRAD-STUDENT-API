package ca.bc.gov.educ.api.gradstudent.constant.v1;

import lombok.Getter;

@Getter
public enum StudentCourseSearchReportHeader {

    PEN("PEN"),
    STUDENT_STATUS("Student Status"),
    SURNAME("Surname"),
    BIRTHDATE("Birthdate"),
    GRADE("Grade"),
    PROGRAM("Program"),
    COMPLETION_DATE("Completion Date"),
    SCHOOL_OF_RECORD_CODE("School of Record Code"),
    SCHOOL_OF_RECORD_NAME("School of Record Name"),
    SCHOOL_OF_GRADUATION_CODE("School of Graduation Code"),
    SCHOOL_OF_GRADUATION_NAME("School of Graduation Name"),
    COURSE_CODE("Course Code"),
    COURSE_LEVEL("Course Level"),
    COURSE_SESSION("Course Session"),
    INTERIM_PERCENT("Interim %"),
    INTERIM_LG("Interim LG"),
    FINAL_PERCENT("Final %"),
    FINAL_LG("Final LG"),
    CREDITS("Credits"),
    EQUIV_CHALL("Equiv. Chall."),
    FINE_ARTS_APP_SKILL("Fine Arts App. Skill"),
    HAS_EXAM("Has Exam?")
    ;

    private final String code;
    StudentCourseSearchReportHeader(String code) { this.code = code; }
}

