package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse;

import ca.bc.gov.educ.api.gradstudent.constant.StudentCourseValidationIssueTypeCode;
import ca.bc.gov.educ.api.gradstudent.constant.ValidationIssueSeverityCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseRuleData;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValidationIssue;
import ca.bc.gov.educ.api.gradstudent.validator.rules.ValidationBaseRule;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

public interface StudentCourseValidationBaseRule extends ValidationBaseRule<StudentCourseRuleData, ValidationIssue> {

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

}
