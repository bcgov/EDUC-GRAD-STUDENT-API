package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Total no of student course rules: 21
 * StudentCourseRule : Status Check
 * Addressed in StudentCourseRule : 1, 3, 4, 5, 6, 8
 * Addressed in StudentCourseExaminableRule : 7
 * Addressed in StudentCoursePercentileRule : 9, 10, 11, 12, 13, 14, 15(NA), 16
 * Addressed in StudentProgramCreditRule(This component) : 17, 18(NA), 19, 20, 21
 * This component covers the listed rules in order listed
 * 17. EThe number of credits must be equal to at least one of the Course Allowable Credits that were available for the course session.
 * 19. User can only enter a Fine Arts/Applied Skills value of 'B', 'A', or 'F' if the course is Board Authority Authorized or Locally Developed and the student is on the 1995 (1996-EN/1996-PF) graduation program. Otherwise, the user must leave as null/blank.
 * 20. User can only enter a Fine Arts/Applied Skills value of 'B' if the course is Board Authority Authorized and the student is on the 2004-EN/2004-PF, 2018-EN/2018-PF, 2023-EN/2023-PF graduation program. Otherwise, the user must leave as null/blank.
 * 21. For the 1995 (1996-EN/1996-PF) graduation program, check number of credits for Fine Arts/Applied Skills.
 * B - student num credits for course must be 4-credits
 * A/F - student num credits for course must be at least 2-credits
 */
@Component
@Slf4j
@Order(605)
public class StudentProgramCreditRule implements StudentCourseValidationBaseRule {

    @Override
    public boolean shouldExecute(StudentCourseRuleData studentCourseRuleData, List<ValidationIssue> list) {
        return !hasValidationError(list);
    }

    @Override
    public List<ValidationIssue> executeValidation(StudentCourseRuleData studentCourseRuleData) {
        log.debug("Executing StudentProgramCreditRule for student :: {}", studentCourseRuleData.getGraduationStudentRecord().getStudentID());
        final List<ValidationIssue> validationIssues = new ArrayList<>();
        Course course = studentCourseRuleData.getCourse();
        if(course != null) {
            StudentCourse studentCourse = studentCourseRuleData.getStudentCourse();
            if(studentCourse.getCredits() != null && course.getCourseAllowableCredit().stream().filter(x -> x.getCreditValue().equals(studentCourse.getCredits().toString())).findFirst().isEmpty()) {
                validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID));
            }
            if (!validationIssues.isEmpty()) return validationIssues;
            if(StringUtils.isNotEmpty(studentCourse.getFineArtsAppliedSkills())) {
                validationIssues.addAll(validateFineArtsAppliedSkillsProgramCode(studentCourseRuleData.getGraduationStudentRecord().getProgram(), studentCourse, course));
            }
        }
        return validationIssues;
    }

    private List<ValidationIssue> validateFineArtsAppliedSkillsProgramCode(String programCode, StudentCourse studentCourse, Course course) {
        final List<ValidationIssue> programValidationIssues = new ArrayList<>();
        if(StringUtils.isBlank(programCode)) return programValidationIssues;
        String fineArtsAppliedSkillsValue = studentCourse.getFineArtsAppliedSkills();
        if(PROGRAM_CODES_BA_LA.contains(programCode)) {
            if (!(Set.of("B", "A", "F").contains(fineArtsAppliedSkillsValue) && Set.of(BOARD_AUTHORITY_CODE, LOCAL_DEVELOPMENT_CODE).contains(course.getCourseCategory().getCode()))) {
                programValidationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_VALID));
            }
            if("B".equals(fineArtsAppliedSkillsValue) && studentCourse.getCredits() != 4) {
                programValidationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID));
            }
            if(Set.of( "A", "F").contains(fineArtsAppliedSkillsValue) && studentCourse.getCredits() < 2) {
                programValidationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID));
            }
        }
        if(PROGRAM_CODES_BA.contains(programCode) && !("B".equals(fineArtsAppliedSkillsValue) && BOARD_AUTHORITY_CODE.equals(course.getCourseCategory().getCode()))) {
            programValidationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_VALID));
        }
        return programValidationIssues;
    }

}
