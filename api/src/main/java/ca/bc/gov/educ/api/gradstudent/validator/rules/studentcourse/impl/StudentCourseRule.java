package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.impl;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.Course;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseRuleData;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValidationIssue;
import ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.UpsertStudentCourseValidationBaseRule;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class StudentCourseRule implements UpsertStudentCourseValidationBaseRule {

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
        Course relatedCourse = studentCourseRuleData.getRelatedCourse();
        if(StringUtils.isBlank(studentCourse.getCourseID()) ||  StringUtils.isBlank(studentCourse.getCourseSession())) {
            return List.of(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INVALID_DATA));
        }
        if (course == null) {
            return List.of(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_VALID));
        }
        if(StringUtils.isNotBlank(studentCourse.getRelatedCourseId()) && relatedCourse == null) {
            return List.of(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_RELATED_COURSE_VALID));
        }
        if(!isValidSessionMonth(studentCourse.getCourseSession())) {
            return List.of(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_MONTH_VALID));
        }
        LocalDate sessionDate = getSessionDate(studentCourse);
        LocalDate sessionDatePlusOne = sessionDate.plusDays(1);
        if (course.getStartDate() != null && sessionDatePlusOne.isBefore(course.getStartDate())) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_START_VALID));
        }
        if (course.getCompletionEndDate() != null && sessionDatePlusOne.isAfter(course.getCompletionEndDate())) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_END_VALID));
        }
        if (isAfterCurrentReportingPeriodEndDate(sessionDate) || sessionDate.isBefore(COURSE_SESSION_MIN_DATE)) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_SESSION_VALID));
        }
        if (course.getCourseCode().startsWith("Q")) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_Q_VALID));
        }
        return validationIssues;
    }

    private boolean isAfterCurrentReportingPeriodEndDate(LocalDate sessionDate) {
        Pair<LocalDate, LocalDate> sessionPeriod = getCurrentSessionPeriod();
        return sessionDate.isAfter(sessionPeriod.getRight());
    }

    private boolean isValidSessionMonth(String dateValue) {
        if(StringUtils.isNotBlank(dateValue) && (dateValue.length() == 6 || (dateValue.length() == 7 && dateValue.charAt(4) == '-'))) {
            Integer monthValue = Integer.valueOf(org.apache.commons.lang3.StringUtils.right(dateValue,2));
            if(monthValue >= 1 && monthValue <= 12) {
                return true;
            }
        }
        return false;
    }

}
