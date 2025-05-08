package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import lombok.extern.slf4j.Slf4j;
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
public class StudentCourseExaminableRule implements StudentCourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentCourseRuleData studentCourseRuleData, List<ValidationIssue> list) {
        return !hasValidationError(list);
    }

    @Override
    public List<ValidationIssue> executeValidation(StudentCourseRuleData studentCourseRuleData) {
        final List<ValidationIssue> validationIssues = new ArrayList<>();
        Course course = studentCourseRuleData.getCourse();
        if(course != null) {
            List<ExaminableCourse>  examinableCourses = studentCourseRuleData.getExaminableCourses();
            LocalDate sessionDate = getSessionDate(studentCourseRuleData.getStudentCourse());
            boolean isExaminable = false;
            for (ExaminableCourse examinableCourse: examinableCourses) {
                if(sessionDate.isAfter(getLocalDate(examinableCourse.getExaminableStart()))
                        && (examinableCourse.getExaminableEnd() != null && sessionDate.isBefore(getLocalDate(examinableCourse.getExaminableStart()))) && !isExaminable) {
                    isExaminable = true;
                }
            }
            if(isExaminable) {
                validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAMINABLE_VALID));
            }

        }

        return validationIssues;
    }
}
