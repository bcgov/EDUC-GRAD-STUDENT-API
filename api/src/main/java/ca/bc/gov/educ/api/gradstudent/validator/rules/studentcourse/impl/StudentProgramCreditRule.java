package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.impl;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.Course;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseRuleData;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValidationIssue;
import ca.bc.gov.educ.api.gradstudent.service.CourseCacheService;
import ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.UpsertStudentCourseValidationBaseRule;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class StudentProgramCreditRule implements UpsertStudentCourseValidationBaseRule {

    private final CourseCacheService courseCacheService;

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
            boolean isZeroCreditsAllowed = studentCourse.getCredits() != null && studentCourse.getCredits() == 0
                    && StringUtils.isNotBlank(studentCourse.getFinalLetterGrade())
                    && (studentCourse.getFinalLetterGrade().equalsIgnoreCase("W") || studentCourse.getFinalLetterGrade().equalsIgnoreCase("F"));

            if(studentCourse.getCredits() != null && !isZeroCreditsAllowed
                    && course.getCourseAllowableCredit().stream().filter(x -> x.getCreditValue().equals(studentCourse.getCredits().toString())).findFirst().isEmpty()) {
                validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_VALID));
            }
            if (!validationIssues.isEmpty()) return validationIssues;
            if(!isValidFlagValue(studentCourse.getFineArtsAppliedSkills())) {
                validationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_VALID));
            }
            if (!validationIssues.isEmpty()) return validationIssues;
            validationIssues.addAll(validateFineArtsAppliedSkillsProgramCode(studentCourseRuleData.getGraduationStudentRecord().getProgram(), studentCourse, course));
        }
        return validationIssues;
    }

    private List<ValidationIssue> validateFineArtsAppliedSkillsProgramCode(String programCode, StudentCourse studentCourse, Course course) {
        final List<ValidationIssue> programValidationIssues = new ArrayList<>();
        if(StringUtils.isBlank(programCode)) return programValidationIssues;
        String fineArtsAppliedSkillsValue = studentCourse.getFineArtsAppliedSkills();
        String courseCode = course.getCourseCode();
        boolean isLevelGrade11 = StringUtils.isNotBlank(course.getCourseLevel()) && course.getCourseLevel().startsWith("11");
        if(StringUtils.isNotBlank(fineArtsAppliedSkillsValue)  && !(isLevelGrade11 && Set.of("B", "A", "F").contains(fineArtsAppliedSkillsValue) && (BOARD_AUTHORITY_CODE.equals(course.getCourseCategory().getCode()) ||
                (courseCode != null && courseCode.startsWith("X")) ||
                CAREER_PROGRAM_CODE.equals(course.getCourseCategory().getCode())))) {
            return List.of(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_BA_LA_CA_VALID));
        }
        if(!PROGRAM_CODES_1995.contains(programCode) && StringUtils.isNotBlank(fineArtsAppliedSkillsValue) && Set.of("B", "A", "F").contains(fineArtsAppliedSkillsValue)
                && (CAREER_PROGRAM_CODE.equalsIgnoreCase(course.getCourseCategory().getCode())
                || (courseCode != null && courseCode.startsWith("X")))) {
                programValidationIssues.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_FINE_ARTS_APPLIED_SKILLED_1995_VALID));
        }
        if (PROGRAM_CODES_1995.contains(programCode)) {
            programValidationIssues.addAll(validate1995FineArtsAppliedSkillsProgramCode(studentCourse));
        }
        return programValidationIssues;
    }

    private List<ValidationIssue> validate1995FineArtsAppliedSkillsProgramCode(StudentCourse studentCourse) {
        final List<ValidationIssue> programValidationIssues1995 = new ArrayList<>();
        String fineArtsAppliedSkillsValue = studentCourse.getFineArtsAppliedSkills();
        if(StringUtils.isNotBlank(fineArtsAppliedSkillsValue)) {
            if("B".equals(fineArtsAppliedSkillsValue) && ((studentCourse.getCredits() != null && studentCourse.getCredits() != 4) || studentCourse.getCredits() == null)) {
                programValidationIssues1995.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_BA_VALID));
            }

            if(Set.of( "A", "F").contains(fineArtsAppliedSkillsValue) && ((studentCourse.getCredits() != null && studentCourse.getCredits() < 2) || studentCourse.getCredits() == null)) {
                programValidationIssues1995.add(createValidationIssue(StudentCourseValidationIssueTypeCode.STUDENT_COURSE_CREDITS_A_F_VALID));
            }
        }
        return programValidationIssues1995;
    }

    private boolean isValidFlagValue(String code) {
        if(StringUtils.isNotBlank(code)) {
            return courseCacheService.getFineArtsAppliedSkillsCodesFromCache().stream().anyMatch(fineArtsAppliedSkillsCode -> fineArtsAppliedSkillsCode.getFineArtsAppliedSkillsCode().equals(code));
        }
        return true;
    }

}
