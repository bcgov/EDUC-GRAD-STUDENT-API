package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.impl;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseRuleData;
import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;

import ca.bc.gov.educ.api.gradstudent.model.dto.ValidationIssue;
import ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.DeleteStudentCourseValidationBaseRule;
import ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.UpsertStudentCourseValidationBaseRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Total no of student course rules: 21
 * StudentCourseRule(This component) : Status Check
 * Addressed in StudentCourseRule : 1, 3, 4, 5, 6, 8
 * Addressed in StudentCourseExaminableRule : 7
 * Addressed in StudentCoursePercentileRule : 9, 10, 11, 12, 13, 14, 15(NA), 16
 * Addressed in StudentProgramCreditRule : 17, 18(NA), 19, 20, 21
 * This component encapsulates student specific validations: (STATUS)
 */
@Component
@Slf4j
@Order(601)
public class StudentStatusRule implements UpsertStudentCourseValidationBaseRule, DeleteStudentCourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentCourseRuleData studentCourseRuleData, List<ValidationIssue> list) {
        return true;
    }

    @Override
    public List<ValidationIssue> executeValidation(StudentCourseRuleData studentCourseRuleData) {
        log.debug("Executing StudentStatusRule for student :: {}", studentCourseRuleData.getGraduationStudentRecord().getStudentID());
        final List<ValidationIssue> validationIssues = new ArrayList<>();
        if(studentCourseRuleData.getGraduationStudentRecord() != null)
        {
            StudentCourseValidationIssueTypeCode issueType = switch (studentCourseRuleData.getGraduationStudentRecord().getStudentStatus()) {
                case "MER" -> StudentCourseValidationIssueTypeCode.STUDENT_STATUS_MER;
                case "DEC" -> StudentCourseValidationIssueTypeCode.STUDENT_STATUS_DEC;
                default -> null;
            };
            if(issueType != null) {
                validationIssues.add(createValidationIssue(issueType));
            }
        }
        return validationIssues;
    }
}
