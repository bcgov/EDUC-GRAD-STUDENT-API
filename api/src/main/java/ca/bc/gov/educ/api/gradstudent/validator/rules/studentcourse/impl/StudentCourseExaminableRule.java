package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.impl;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.constant.ValidationIssueSeverityCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.service.CourseCacheService;
import ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.UpsertStudentCourseValidationBaseRule;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Total no of student course rules: 21
 * StudentCourseRule : Status Check
 * Addressed in StudentCourseRule : 1, 3, 4, 5, 6, 8
 * Addressed in StudentCourseExaminableRule : 7
 * Addressed in StudentCoursePercentileRule(This component) : 9, 10, 11, 12, 13, 14, 15(NA), 16
 * Addressed in StudentProgramCreditRule : 17, 18(NA), 19, 20, 21
 * This component covers the listed rules in order listed
 * 7. Courses that required an exam at the time of the course session date cannot be entered as non-examinable
 */
@Component
@Slf4j
@Order(603)
@AllArgsConstructor
public class StudentCourseExaminableRule implements UpsertStudentCourseValidationBaseRule {

    private final CourseCacheService courseCacheService;

    @Override
    public boolean shouldExecute(StudentCourseRuleData studentCourseRuleData, List<ValidationIssue> list) {
        return !hasValidationError(list);
    }

    @Override
    public List<ValidationIssue> executeValidation(StudentCourseRuleData studentCourseRuleData) {
        log.debug("Executing StudentCourseExaminableRule for student :: {}", studentCourseRuleData.getGraduationStudentRecord().getStudentID());
        final List<ValidationIssue> validationIssues = new ArrayList<>();
        StudentCourse studentCourse = studentCourseRuleData.getStudentCourse();
        StudentCourseExam studentCourseExam =studentCourse.getCourseExam();
        Course course = studentCourseRuleData.getCourse();
        ExaminableCourse examinableCourse = getExaminableCourse(course, getProgramYear(studentCourseRuleData.getGraduationStudentRecord().getProgram()));
        LocalDate sessionDate = getSessionDate(studentCourse);
        boolean isCourseMarkedExaminable = studentCourseExam != null;
        boolean isCourseActualExaminable = examinableCourse != null;
        boolean isCourseActualExaminableMandatory = isCourseActualExaminable && isExaminableMandatory(sessionDate, examinableCourse);
        if(!isCourseMarkedExaminable && isCourseActualExaminableMandatory) {
            ValidationIssueSeverityCode severityCode = Boolean.TRUE.equals(studentCourseRuleData.getIsSystemCoordinator()) ? ValidationIssueSeverityCode.WARNING: ValidationIssueSeverityCode.ERROR;
            validationIssues.add(createValidationIssue(severityCode, StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_VALID));
        }
        if(isCourseMarkedExaminable && !isCourseActualExaminableMandatory) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_MANDATORY_VALID));
        }
        return validationIssues;
    }

    private boolean isExaminableMandatory(LocalDate sessionDate, ExaminableCourse examinableCourse) {
        LocalDate mandatoryStart = getAsDefaultLocalDate(examinableCourse.getExaminableStart());
        LocalDate mandatoryEnd = getAsDefaultLocalDate(examinableCourse.getExaminableEnd());
        if(mandatoryStart == null) return false;
        return isDateWithinRange(sessionDate, mandatoryStart, mandatoryEnd);
    }

    private String getProgramYear(String programCode) {
        return StringUtils.isNotBlank(programCode)? StringUtils.left(programCode, 4): "DEFAULT";
    }

    private ExaminableCourse getExaminableCourse(Course course, String programYear) {
        if(courseCacheService.getExaminableCoursesFromCacheByProgramYear(programYear).isEmpty()) {
            log.warn("No examinable courses found in cache for program year: {}", programYear);
            return null;
        }
        return courseCacheService.getExaminableCoursesFromCacheByProgramYear(programYear).stream().filter(examinableCourse -> examinableCourse.getCourseCode().equals(course.getCourseCode()) && (examinableCourse.getCourseLevel().equals(course.getCourseLevel()))).findFirst().orElse(null);
    }
}
