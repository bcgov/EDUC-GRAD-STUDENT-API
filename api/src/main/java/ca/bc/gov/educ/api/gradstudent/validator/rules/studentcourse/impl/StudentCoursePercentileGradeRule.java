package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.impl;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
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
 * 9. Interim percent cannot be negative
 * 10. Interim percent cannot be greater than 100
 * 11. If User enters zero or skips Interim PCT (i.e. they tab to Interim Letter Grade), they are prompted for an Interim Letter Grade.
 *     User can tab through this field but if they enter an interim Letter Grade it must be a valid letter grade based on the letter grade validation rules.
 * 12. Final percent cannot be negative
 * 13. Final percent cannot be greater than 100
 * 14. If User skips Final PCT (i.e. they tab to Final Letter Grade)
 *     or course session is prior to 199409 (i.e. no Final percent required for these courses), they are prompted for a Final Letter Grade
 *     Users must enter a value here.  Final Letter Grade must be a valid letter grade based on the letter grade validation rules.
 * 16. Final pct or Final Letter Grade should be included for completed courses - Course session has passed with no final mark
 */
@Component
@Slf4j
@Order(604)
@AllArgsConstructor
public class StudentCoursePercentileGradeRule implements UpsertStudentCourseValidationBaseRule {

    private final CourseCacheService courseCacheService;

    @Override
    public boolean shouldExecute(StudentCourseRuleData studentCourseRuleData, List<ValidationIssue> list) {
        return !hasValidationError(list);
    }

    @Override
    public List<ValidationIssue> executeValidation(StudentCourseRuleData studentCourseRuleData) {
        log.debug("Executing StudentCoursePercentileGradeRule for student :: {}", studentCourseRuleData.getGraduationStudentRecord().getStudentID());
        final List<ValidationIssue> validationIssues = new ArrayList<>();
        StudentCourse studentCourse = studentCourseRuleData.getStudentCourse();
        if(!isAcceptablePercentile(studentCourse.getInterimPercent())) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_PERCENT_VALID));
        }
        if(!isAcceptablePercentile(studentCourse.getFinalPercent())) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_VALID));
        }
        if(getSessionDate(studentCourse).isBefore(LocalDate.now()) && studentCourse.getFinalPercent() == null && StringUtils.isBlank(studentCourse.getFinalLetterGrade())) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_PERCENT_GRADE_VALID));
        }
        if (!validationIssues.isEmpty()) return validationIssues;
        //Logic for LetterGrade - Begin
        boolean isPercentageMandatory  = isPercentageMandatory(studentCourseRuleData);
        if(!validateInterimLetterGrade(studentCourse, isPercentageMandatory)) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_INTERIM_GRADE_VALID));
        }
        if(!validateFinalLetterGrade(studentCourseRuleData, isPercentageMandatory)) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINAL_GRADE_VALID));
        }
        //Logic for LetterGrade - End
        return validationIssues;
    }

    private boolean validateInterimLetterGrade(StudentCourse studentCourse, Boolean isPercentageMandatory) {
        if(StringUtils.isNotBlank(studentCourse.getInterimLetterGrade())) {
            return validateLetterGrade(getSessionDate(studentCourse),  getPercentValue(studentCourse.getInterimPercent(), isPercentageMandatory) , getLetterGrade(studentCourse.getInterimLetterGrade()));
        }
        return true;
    }

    private boolean validateFinalLetterGrade(StudentCourseRuleData studentCourseRuleData,Boolean isPercentageMandatory) {
        StudentCourse studentCourse = studentCourseRuleData.getStudentCourse();
        if(StringUtils.isNotBlank(studentCourse.getFinalLetterGrade())) {
            return validateLetterGrade(getSessionDate(studentCourse),  getPercentValue(studentCourse.getFinalPercent(), isPercentageMandatory) , getLetterGrade(studentCourse.getFinalLetterGrade()));
        }
        return true;
    }

    private Integer getPercentValue(Integer percent, Boolean isPercentageMandatory) {
        if(percent != null) return percent;
        return Boolean.TRUE.equals(isPercentageMandatory) ? DEFAULT_MIN_PERCENTAGE_VALUE : null;
    }

    private boolean validateLetterGrade(LocalDate sessionDate, Integer percent, LetterGrade letterGrade) {
        if(letterGrade != null) {
            return isDateWithinRange(sessionDate, getLocalDate(letterGrade.getEffectiveDate()), getLocalDate(letterGrade.getExpiryDate())) &&
                    (percent == null || (letterGrade.getPercentRangeLow() != null && letterGrade.getPercentRangeHigh() != null && letterGrade.getPercentRangeLow() <= percent && letterGrade.getPercentRangeHigh() >= percent));
        }
        return true;
    }

    private boolean isPercentageMandatory(StudentCourseRuleData studentCourseRuleData) {
        StudentCourse studentCourse = studentCourseRuleData.getStudentCourse();
        LocalDate sessionDate = getSessionDate(studentCourse);
        if (sessionDate.isBefore(LEGISLATION_PERCENT_MANDATORY_DATE)) {
            return false;
        }
        if (sessionDate.isAfter(LEGISLATION_PERCENT_MANDATORY_DATE)) {
            Course course = studentCourseRuleData.getCourse();
            String studentProgram = studentCourseRuleData.getGraduationStudentRecord().getProgram();
            if (StringUtils.isNotBlank(studentProgram) && PROGRAM_CODES_1995.contains(studentProgram) && "10".equals(course.getCourseLevel())) {
                return false;
            }
            LetterGrade interimLetterGrade = getLetterGrade(studentCourse.getInterimLetterGrade());
            LetterGrade finalLetterGrade = getLetterGrade(studentCourse.getFinalLetterGrade());
            if (interimLetterGrade != null && interimLetterGrade.getPercentRangeLow() == null && interimLetterGrade.getPercentRangeHigh() == null) {
                return false;
            }
            if (finalLetterGrade != null && finalLetterGrade.getPercentRangeLow() == null && finalLetterGrade.getPercentRangeHigh() == null) {
                return false;
            }
        }
        return true;
    }

    private LetterGrade getLetterGrade(String grade) {
        return StringUtils.isNotBlank(grade) ? courseCacheService.getLetterGradesFromCache().stream().filter(letterGrade -> letterGrade.getGrade().equals(grade)).findFirst().orElse(null) : null;
    }

}
