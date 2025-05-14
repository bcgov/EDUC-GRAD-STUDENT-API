package ca.bc.gov.educ.api.gradstudent.validator.rules;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseRuleData;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseValidationIssue;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValidationIssue;
import ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.StudentCourseValidationBaseRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class StudentCourseRulesProcessor {
    private final List<StudentCourseValidationBaseRule> rules;

    @Autowired
    public StudentCourseRulesProcessor(final List<StudentCourseValidationBaseRule> rules) {
        this.rules = rules;
    }

    public List<ValidationIssue> processRules(StudentCourseRuleData studentCourseRuleData) {
        final List<ValidationIssue> validationErrorsMap = new ArrayList<>();
        log.debug("Starting course validations check for student :: {}", studentCourseRuleData.getGraduationStudentRecord().getStudentID());
        rules.forEach(rule -> {
            if(rule.shouldExecute(studentCourseRuleData, validationErrorsMap)) {
                validationErrorsMap.addAll(rule.executeValidation(studentCourseRuleData));
            }
        });
        return validationErrorsMap;
    }
}
