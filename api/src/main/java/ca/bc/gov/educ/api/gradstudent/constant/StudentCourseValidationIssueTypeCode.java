package ca.bc.gov.educ.api.gradstudent.constant;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public enum StudentCourseValidationIssueTypeCode {

    STUDENT_STATUS_MER("studentStatus", "This student is showing as merged.  Student Course / Exam Data cannot be updated for students with a status of \"MER\" merged", ValidationIssueSeverityCode.ERROR),
    STUDENT_STATUS_TER("studentStatus", "This student has been terminated.  Re-activate by setting their status to \"CUR\" if they are currently attending school", ValidationIssueSeverityCode.WARNING),
    STUDENT_STATUS_ARC("studentStatus", "This student is not active.  Re-activate by setting their status to \"CUR\" if they are currently attending school", ValidationIssueSeverityCode.WARNING),
    STUDENT_STATUS_DEC("studentStatus", "This student is showing as deceased.", ValidationIssueSeverityCode.WARNING),

    STUDENT_COURSE_DUPLICATE("course", "The course session is a duplicate of an existing course session for this student" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_UPDATE_NOT_FOUND("course", "Invalid Course - course code/level does not exist for this student" , ValidationIssueSeverityCode.ERROR),

    STUDENT_COURSE_INVALID_DATA("course", "Invalid Course ID/Session - course id and session cannot be empty or blank" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_VALID("course", "Invalid Course code/level - course code/level does not exist in the ministry course registry" , ValidationIssueSeverityCode.ERROR),
    STUDENT_RELATED_COURSE_VALID("relatedCourse", "Invalid Course code/level - course code/level does not exist in the ministry course registry" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_SESSION_MONTH_VALID("course", "Course session month must be between 01 and 12" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_SESSION_START_VALID("course", "Course session is before the course start date" , ValidationIssueSeverityCode.WARNING),
    STUDENT_COURSE_SESSION_END_VALID("course", "Course session is after the course completion date" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_SESSION_VALID("course", "Course session cannot be beyond the current reporting period or prior to 198401" , ValidationIssueSeverityCode.WARNING),
    STUDENT_COURSE_Q_VALID("course", "Only use Q code if student was on Adult program at time of course completion or if course is marked as Equivalency" , ValidationIssueSeverityCode.WARNING),

    STUDENT_COURSE_EXAMINABLE_VALID("course","This course required an exam at the time of the course session date",ValidationIssueSeverityCode.ERROR),

    STUDENT_COURSE_INTERIM_PERCENT_VALID("interimPercent", "Interim percent cannot be negative or greater than 100" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_INTERIM_GRADE_VALID("interimLetterGrade", "Invalid Letter Grade" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_FINAL_PERCENT_VALID("finalPercent", "Final percent cannot be negative or greater than 100" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_FINAL_GRADE_VALID("finalLetterGrade", "Invalid Letter Grade" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID("finalLetterGrade", "Course session is in the past. Enter a final mark" , ValidationIssueSeverityCode.ERROR),

    STUDENT_COURSE_EXAM_VALID("course","This course required an exam at the time of the course session date", ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_EXAM_MANDATORY_VALID("course","This course is not showing as examinable during this session. Please enter this course as non-examinable", ValidationIssueSeverityCode.WARNING),
    STUDENT_COURSE_EXAM_OPTIONAL_VALID("course","This exam for this course showing as optional during this session", ValidationIssueSeverityCode.WARNING),

    STUDENT_COURSE_EXAM_SCHOOL_PERCENT_VALID("schoolPercentage","School percent cannot be null, negative or greater than 100", ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_EXAM_BEST_SCHOOL_PERCENT_VALID("bestSchoolPercentage","Best School percent cannot be null, negative or greater than 100", ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_EXAM_BEST_PERCENT_VALID("examPercentage","Best Exam percent cannot be negative or greater than 100", ValidationIssueSeverityCode.ERROR),

    STUDENT_COURSE_EXAM_SPECIAL_CASE_AEGROTAT_VALID("specialCase","Special Case can only be set as Aegrotat", ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_EXAM_SPECIAL_CASE_VALID("specialCase","Invalid Special Case Code", ValidationIssueSeverityCode.ERROR),

    STUDENT_COURSE_EQUIVALENCY_CHALLENGE_VALID("equivOrChallenge","Invalid Equivalency or Challenge Code", ValidationIssueSeverityCode.ERROR),

    STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_CA_VALID("fineArtsAppliedSkills", "Flag is not applicable to this course type" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_VALID("fineArtsAppliedSkills", "Invalid Fine Arts Applied Skills Code" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_1995_VALID("fineArtsAppliedSkills", "Flag is only applicable for this course type if student on the 1995 program" , ValidationIssueSeverityCode.WARNING),
    STUDENT_COURSE_CREDITS_BA_VALID("credits", "Number of Credits must be 4 if B has been selected for the Board Authority Authorized or Locally Developed course Fine Arts/Applied Skills flag" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_CREDITS_A_F_VALID("credits", "Number of Credits must at least 2 if A or F has been selected for the Board Authority Authorized or Locally Developed course Fine Arts/Applied Skills flag" , ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_CREDITS_VALID("credits", "The number of credits is not an allowable credit value in the Course Registry", ValidationIssueSeverityCode.ERROR),

    STUDENT_COURSE_DELETE_GRADUATION_VALID("course", "Graduated Student - ensure this course can be deleted without removing any graduation or optional program requirements", ValidationIssueSeverityCode.WARNING),
    STUDENT_COURSE_DELETE_EXAM_VALID("course", "This course has an associated exam record", ValidationIssueSeverityCode.ERROR),

    STUDENT_COURSE_TRANSFER_SAME_STUDENT("targetStudentId", "Source student and target student are the same", ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_TRANSFER_STUDENT_COURSE_MISMATCH("studentCourseIdsToMove", "Course does not belong to source student", ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_NOT_FOUND("studentCourseIdsToMove", "Student Course not found", ValidationIssueSeverityCode.ERROR),
    STUDENT_COURSE_TRANSFER_COURSE_DUPLICATE("studentCourseIdsToMove", "The course session is a duplicate of an existing course session for the target student", ValidationIssueSeverityCode.ERROR),;



    private static final Map<String, StudentCourseValidationIssueTypeCode> CODE_MAP = new HashMap<>();

    static {
        for (StudentCourseValidationIssueTypeCode type : values()) {
            CODE_MAP.put(type.getCode(), type);
        }
    }

    /**
     * The Code.
     */
    @Getter
    private final String code;

    /**
     * Validation message
     */
    @Getter
    private final String message;

    @Getter
    private final ValidationIssueSeverityCode severityCode;

    /**
     * Instantiates a new Pen request batch student validation issue type code.
     *
     * @param code the code
     */
    StudentCourseValidationIssueTypeCode(String code, String message, ValidationIssueSeverityCode severityCode) {
        this.code = code;
        this.message = message;
        this.severityCode = severityCode;
    }
    public static StudentCourseValidationIssueTypeCode findByValue(String value) {
        return CODE_MAP.get(value);
    }
}
