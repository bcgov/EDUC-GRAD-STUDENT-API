package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.impl;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseActivityType;
import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.service.CourseCacheService;
import ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.UpsertStudentCourseValidationBaseRule;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Order(607)
@AllArgsConstructor
public class StudentCourseExamSpecialCaseRule implements UpsertStudentCourseValidationBaseRule {

    private final CourseCacheService courseCacheService;

    @Override
    public boolean shouldExecute(StudentCourseRuleData studentCourseRuleData, List<ValidationIssue> list) {
        return !hasValidationError(list);
    }

    @Override
    public List<ValidationIssue> executeValidation(StudentCourseRuleData studentCourseRuleData) {
        log.debug("Executing StudentCourseExamSpecialCaseRule for student :: {}", studentCourseRuleData.getGraduationStudentRecord().getStudentID());
        final List<ValidationIssue> validationIssues = new ArrayList<>();
        StudentCourse studentCourse = studentCourseRuleData.getStudentCourse();
        StudentCourseExam studentCourseExam = studentCourse.getCourseExam();
        boolean isUpdate = studentCourseRuleData.getActivityType() != null && StudentCourseActivityType.USERCOURSEMOD.equals(studentCourseRuleData.getActivityType());
        if(studentCourseExam != null && !validateSpecialCaseCode(studentCourseExam.getSpecialCase(), isUpdate)) {
            validationIssues.add(createValidationIssue(
                    !isUpdate ? StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SPECIAL_CASE_AEGROTAT_VALID : StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EXAM_SPECIAL_CASE_VALID));
        }

        return validationIssues;
    }

    private boolean validateSpecialCaseCode(String specialCaseCode, boolean isUpdate) {
        if(StringUtils.isNotBlank(specialCaseCode)) {
            ExamSpecialCaseCode specialCase =  getExamSpecialCaseByCode(specialCaseCode);
            if(specialCase == null || (!isUpdate && !specialCase.getExamSpecialCaseCode().equals("A"))) {
                return false;
            }
        }
        return true;
    }

    private ExamSpecialCaseCode getExamSpecialCaseByCode(String code) {
        return StringUtils.isNotBlank(code) ? courseCacheService.getExamSpecialCaseCodesFromCache().stream().filter(examSpecialCase -> examSpecialCase.getExamSpecialCaseCode().equals(code)).findFirst().orElse(null) : null;
    }
}
