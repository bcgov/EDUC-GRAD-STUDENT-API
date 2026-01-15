package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse;

import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDate;
import java.util.Set;

public interface UpsertStudentCourseValidationBaseRule extends StudentCourseValidationBaseRule {

    Set<String> PROGRAM_CODES_1995 = Set.of("1996-EN","1996-PF"); //1995 program codes
    LocalDate COURSE_SESSION_MIN_DATE = LocalDate.parse("1984-01-01");
    LocalDate LEGISLATION_PERCENT_MANDATORY_DATE = LocalDate.parse("1994-09-01");
    Integer DEFAULT_MIN_PERCENTAGE_VALUE = 0;
    Integer DEFAULT_MAX_PERCENTAGE_VALUE = 100;
    String BOARD_AUTHORITY_CODE = "BA";
    String CAREER_PROGRAM_CODE = "CP";

    default Pair<LocalDate, LocalDate> getCurrentSessionPeriod() {
        LocalDate now = LocalDate.now();
        // If we're in October or later, the reporting period is Oct 1 (current year) to Sep 30 (next year)
        // If we're before October, the reporting period is Oct 1 (last year) to Sep 30 (current year)
        int startYear = now.getMonthValue() >= 10 ? now.getYear() : now.getYear() - 1;
        int endYear = startYear + 1;
        return Pair.of(getLocalDateFromString(startYear + "-10-01"), getLocalDateFromString(endYear + "-09-30"));
    }

    default boolean isAcceptablePercentile(Integer value) {
        return value == null || (value >= DEFAULT_MIN_PERCENTAGE_VALUE && value <= DEFAULT_MAX_PERCENTAGE_VALUE);
    }

    default boolean isDateWithinRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        return date != null && (startDate == null || !date.isBefore(startDate)) &&
                (endDate == null || !date.isAfter(endDate));
    }
}
