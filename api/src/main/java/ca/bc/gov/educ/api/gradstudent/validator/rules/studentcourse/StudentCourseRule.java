package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.Course;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseRuleData;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValidationIssue;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Total no of student course rules: 21
 * StudentCourseRule : Status Check
 * Addressed in StudentCourseRule(This component) : 1, 3, 4, 5, 6, 8
 * Addressed in StudentCourseExaminableRule : 7
 * Addressed in StudentCoursePercentileRule : 9, 10, 11, 12, 13, 14, 15(NA), 16
 * Addressed in StudentProgramCreditRule : 17, 18(NA), 19, 20, 21
 * This component covers the listed rules in order listed
 * 1. Entering a valid course code and course level
 * 3. Course session month must be between 01 and 12 (Covered as date)
 * 4. Course session date plus day of 01 should not be before the course start date
 * 5. Course session date plus day of 01 cannot be after the course completion date
 * 6. Course session must be no greater than current reporting period or no less than 198401
 * 8. Post Q-code warning
 */
@Component
@Slf4j
@Order(602)
public class StudentCourseRule implements StudentCourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentCourseRuleData studentCourseRuleData, List<ValidationIssue> list) {
        return !hasValidationError(list);
    }

    @Override
    public List<ValidationIssue> executeValidation(StudentCourseRuleData studentCourseRuleData) {
        log.debug("Executing StudentCourseRule for student :: {}", studentCourseRuleData.getGraduationStudentRecord().getStudentID());
        final List<ValidationIssue> validationIssues = new ArrayList<>();
        StudentCourse studentCourse = studentCourseRuleData.getStudentCourse();
        Course course = studentCourseRuleData.getCourse();
        if (course == null) {
            return List.of(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID));
        }
        LocalDate sessionDate = getSessionDate(studentCourse);
        LocalDate sessionDatePlusOne = sessionDate.plusDays(1);
        if (course.getStartDate() != null && sessionDatePlusOne.isBefore(course.getStartDate())) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID));
        }
        if (course.getCompletionEndDate() != null && sessionDatePlusOne.isAfter(course.getCompletionEndDate())) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID));
        }
        if (!isSessionDateInReportingPeriod(sessionDate) || sessionDate.isBefore(COURSE_SESSION_MIN_DATE)) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID));
        }
        if (course.getCourseCode().startsWith("Q")) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID));
        }
        return validationIssues;
    }

    private boolean isSessionDateInReportingPeriod(LocalDate sessionDate) {
        Pair<LocalDate, LocalDate> sessionPeriod = getSessionPeriod(sessionDate);
        return sessionDate.isAfter(sessionPeriod.getLeft()) && sessionDate.isBefore(sessionPeriod.getRight());
    }

}
