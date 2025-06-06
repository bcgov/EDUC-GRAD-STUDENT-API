package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.constant.ValidationIssueSeverityCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.validator.rules.ValidationBaseRule;
import io.micrometer.common.util.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Set;

public interface StudentCourseValidationBaseRule extends ValidationBaseRule<StudentCourseRuleData, ValidationIssue> {

    Set<String> PROGRAM_CODES_BA_LA = Set.of("1996-EN","1996-PF"); //1995 program codes
    Set<String> PROGRAM_CODES_BA = Set.of("2004-EN", "2004-PF", "2018-EN", "2018-PF", "2023-EN","2023-PF");
    LocalDate COURSE_SESSION_MIN_DATE = LocalDate.parse("1984-01-01");
    LocalDate LEGISLATION_PERCENT_MANDATORY_DATE = LocalDate.parse("1994-09-01");
    Integer DEFAULT_MIN_PERCENTAGE_VALUE = 0;
    Integer DEFAULT_MAX_PERCENTAGE_VALUE = 100;
    String BOARD_AUTHORITY_CODE = "BA";
    String LOCAL_DEVELOPMENT_CODE = "LA";

    default ValidationIssue createValidationIssue(ValidationIssueSeverityCode severityCode, StudentCourseValidationIssueTypeCode fieldCode){
        ValidationIssue validationIssue = createValidationIssue(fieldCode);
        validationIssue.setValidationIssueSeverityCode(severityCode.toString());
        return validationIssue;
    }

    default ValidationIssue createValidationIssue(StudentCourseValidationIssueTypeCode fieldCode){
        ValidationIssue validationIssue = new ValidationIssue();
        validationIssue.setValidationFieldName(fieldCode.getCode());
        validationIssue.setValidationIssueSeverityCode(fieldCode.getSeverityCode().getCode());
        validationIssue.setValidationIssueMessage(fieldCode.getMessage());
        return validationIssue;
    }

    default boolean hasValidationError(List<ValidationIssue> validationIssues) {
        return validationIssues.stream().anyMatch(issue -> "ERROR".equals(issue.getValidationIssueSeverityCode()));
    }

    default LocalDate getSessionDate(StudentCourse studentCourse) {
        if(StringUtils.isNotBlank(studentCourse.getCourseSession())) {
            return getAsDefaultLocalDate(studentCourse.getCourseSession());
        }
        return null;
    }

    //This supports format YYYY-MM / YYYYMM
    default LocalDate getAsDefaultLocalDate(String dateValue) {
        if(StringUtils.isNotBlank(dateValue)) {
            if(dateValue.length() == 6) {
                return getLocalDateFromString(dateValue.substring(0,4)+"-"+dateValue.substring(4,6)+"-01");
            } else if(dateValue.length() == 7 && dateValue.charAt(4) == '-') {
                return getLocalDateFromString(dateValue.substring(0,4)+"-"+dateValue.substring(5,7)+"-01");
            }
            return null;
        }
        return null;
    }



    default LocalDate getLocalDateFromString(String localDate) {
        return LocalDate.parse(localDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }

    default LocalDate getLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    default Pair<LocalDate, LocalDate> getCurrentSessionPeriod(LocalDate sessionDate) {
        return Pair.of(getLocalDateFromString(LocalDate.now().minusYears(1).getYear() + "-10-01"), getLocalDateFromString(LocalDate.now().getYear() + "-09-30"));
    }

    default boolean isAcceptablePercentile(Integer value) {
        return value == null || (value >= DEFAULT_MIN_PERCENTAGE_VALUE && value <= DEFAULT_MAX_PERCENTAGE_VALUE);
    }

}
