package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.service.CourseCacheService;
import io.micrometer.common.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
@Order(606)
@AllArgsConstructor
public class StudentCourseEquivalencyChallengeRule implements StudentCourseValidationBaseRule {

    private final CourseCacheService courseCacheService;

    @Override
    public boolean shouldExecute(StudentCourseRuleData studentCourseRuleData, List<ValidationIssue> list) {
        return !hasValidationError(list);
    }

    @Override
    public List<ValidationIssue> executeValidation(StudentCourseRuleData studentCourseRuleData) {
        log.debug("Executing StudentCourseEquivalencyChallengeRule for student :: {}", studentCourseRuleData.getGraduationStudentRecord().getStudentID());
        final List<ValidationIssue> validationIssues = new ArrayList<>();
        StudentCourse studentCourse = studentCourseRuleData.getStudentCourse();
        if(!validateEquivalentOrChallengeCode(studentCourse.getEquivOrChallenge())) {
            validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_EQUIVALENCY_CHALLENGE_VALID));
        }
        return validationIssues;
    }

    private boolean validateEquivalentOrChallengeCode(String equivalentOrChallengeCode) {
        if(StringUtils.isNotBlank(equivalentOrChallengeCode)) {
            EquivalentOrChallengeCode equivalentOrChallenge =  getEquivalentOrChallengeByCode(equivalentOrChallengeCode);
            if(equivalentOrChallenge == null) {
                return false;
            }
        }
        return true;
    }

    private EquivalentOrChallengeCode getEquivalentOrChallengeByCode(String code) {
        return StringUtils.isNotBlank(code) ? courseCacheService.getEquivalentOrChallengeCodesFromCache().stream().filter(equivalentOrChallenge -> equivalentOrChallenge.getEquivalentOrChallengeCode().equals(code)).findFirst().orElse(null) : null;
    }
}
