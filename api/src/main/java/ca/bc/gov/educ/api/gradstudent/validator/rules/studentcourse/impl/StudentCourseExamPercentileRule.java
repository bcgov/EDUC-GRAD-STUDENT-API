package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.impl;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseActivityType;
import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseExam;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseRuleData;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValidationIssue;
import ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.UpsertStudentCourseValidationBaseRule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Order(608)
public class StudentCourseExamPercentileRule implements UpsertStudentCourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentCourseRuleData studentCourseRuleData, List<ValidationIssue> list) {
        return !hasValidationError(list);
    }

    @Override
    public List<ValidationIssue> executeValidation(StudentCourseRuleData studentCourseRuleData) {
        log.debug("Executing StudentCourseExamPercentileRule for student :: {}", studentCourseRuleData.getGraduationStudentRecord().getStudentID());
        final List<ValidationIssue> validationIssues = new ArrayList<>();
        StudentCourse studentCourse = studentCourseRuleData.getStudentCourse();
        StudentCourseExam courseExam = studentCourse.getCourseExam();
        if(courseExam != null) {
            if((isExamAdded(studentCourseRuleData) && courseExam.getSchoolPercentage() == null) || !isAcceptablePercentile(courseExam.getSchoolPercentage())) {
                validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SCHOOL_PERCENT_VALID));
            }
            if((isExamAdded(studentCourseRuleData) && courseExam.getBestSchoolPercentage() == null)  || !isAcceptablePercentile(courseExam.getBestSchoolPercentage())) {
                validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_SCHOOL_PERCENT_VALID));
            }
            if(!isAcceptablePercentile(courseExam.getBestExamPercentage())) {
                validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_BEST_PERCENT_VALID));
            }
        }
        return validationIssues;
    }

    private boolean isExamAdded(StudentCourseRuleData studentCourseRuleData) {
        return studentCourseRuleData.getActivityType().equals(StudentCourseActivityType.USERCOURSEADD)
                || studentCourseRuleData.getActivityType().equals(StudentCourseActivityType.USEREXAMADD);
    }
}
