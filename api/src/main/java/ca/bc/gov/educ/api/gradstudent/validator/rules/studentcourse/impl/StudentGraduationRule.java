package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.impl;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseRuleData;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValidationIssue;
import ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.DeleteStudentCourseValidationBaseRule;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * This rule is used only during DELETE action
 */
@Component
@Slf4j
@Order(600)
public class StudentGraduationRule implements DeleteStudentCourseValidationBaseRule {
    @Override
    public boolean shouldExecute(StudentCourseRuleData studentCourseRuleData, List<ValidationIssue> list) {
        return !hasValidationError(list);
    }

    @Override
    public List<ValidationIssue> executeValidation(StudentCourseRuleData studentCourseRuleData) {
        log.debug("Executing StudentGraduation for student :: {}", studentCourseRuleData.getGraduationStudentRecord().getStudentID());
        final List<ValidationIssue> validationIssues = new ArrayList<>();
        if(studentCourseRuleData.getGraduationStudentRecord() != null)
        {
            boolean isGraduated = StringUtils.isNotBlank(studentCourseRuleData.getGraduationStudentRecord().getProgramCompletionDate());
            if(isGraduated && !"SCCP".equals(studentCourseRuleData.getGraduationStudentRecord().getProgram())) {
                validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_DELETE_GRADUATION_VALID));
            }
        }
        return validationIssues;
    }
}
