package ca.bc.gov.educ.api.gradstudent.validator.rules.studentcourse.impl;

import ca.bc.gov.educ.api.gradstudent.constant.ValidationIssueSeverityCode;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class StudentCourseRuleTest {

    @InjectMocks
    private StudentCourseRule studentCourseRule;

    private static final DateTimeFormatter COURSE_SESSION_FMT = DateTimeFormatter.ofPattern("yyyyMM");

    private StudentCourseRuleData studentCourseRuleData;
    private StudentCourse studentCourse;
    private Course course;

  @Before
    public void setUp() {
        // Initialize test data
    GraduationStudentRecord graduationStudentRecord = new GraduationStudentRecord();
        graduationStudentRecord.setStudentID(UUID.randomUUID());

        studentCourse = StudentCourse.builder()
                .courseID("MATH10")
                .courseSession("202301")
                .build();

        course = Course.builder()
                .courseCode("MATH10")
                .courseLevel("10")
                .startDate(LocalDate.of(2023, 1, 1))
                .completionEndDate(LocalDate.of(2023, 6, 30))
                .build();

        studentCourseRuleData = StudentCourseRuleData.builder()
                .studentCourse(studentCourse)
                .course(course)
                .graduationStudentRecord(graduationStudentRecord)
                .build();
    }

    @Test
    public void testShouldExecute_WhenNoValidationErrors_ShouldReturnTrue() {
        // Given
        List<ValidationIssue> validationIssues = new ArrayList<>();

        // When
        boolean result = studentCourseRule.shouldExecute(studentCourseRuleData, validationIssues);

        // Then
        assertTrue(result);
    }

    @Test
    public void testShouldExecute_WhenHasValidationErrors_ShouldReturnFalse() {
        // Given
        List<ValidationIssue> validationIssues = new ArrayList<>();
        validationIssues.add(ValidationIssue.builder()
                .validationIssueSeverityCode(ValidationIssueSeverityCode.ERROR.getCode())
                .build());

        // When
        boolean result = studentCourseRule.shouldExecute(studentCourseRuleData, validationIssues);

        // Then
        assertFalse(result);
    }

    @Test
    public void testExecuteValidation_WhenCourseIdIsBlank_ShouldReturnInvalidDataError() {
        // Given
        studentCourse.setCourseID("");
        studentCourse.setCourseSession("202301");

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("course");
        assertThat(result.get(0).getValidationIssueMessage()).contains("course id and session cannot be empty");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.ERROR.getCode());
    }

    @Test
    public void testExecuteValidation_WhenCourseSessionIsBlank_ShouldReturnInvalidDataError() {
        // Given
        studentCourse.setCourseID("MATH10");
        studentCourse.setCourseSession("");

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("course");
        assertThat(result.get(0).getValidationIssueMessage()).contains("course id and session cannot be empty");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.ERROR.getCode());
    }

    @Test
    public void testExecuteValidation_WhenCourseIsNull_ShouldReturnCourseValidError() {
        // Given
        studentCourseRuleData.setCourse(null);

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("course");
        assertThat(result.get(0).getValidationIssueMessage()).contains("course code/level does not exist");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.ERROR.getCode());
    }

    @Test
    public void testExecuteValidation_WhenRelatedCourseIdExistsButRelatedCourseIsNull_ShouldReturnRelatedCourseError() {
        // Given
        studentCourse.setRelatedCourseId("RELATED123");
        studentCourseRuleData.setRelatedCourse(null);

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("relatedCourse");
        assertThat(result.get(0).getValidationIssueMessage()).contains("course code/level does not exist");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.ERROR.getCode());
    }

    @Test
    public void testExecuteValidation_WhenSessionDateIsBeforeCourseStartDate_ShouldReturnSessionStartWarning() {
        // Given
        LocalDate currentDate = LocalDate.now();
        LocalDate courseStartDate = currentDate.plusMonths(1);
        LocalDate courseCompletionDate = currentDate.plusMonths(3);
        // Use a session date that's before start date but within current reporting period
        String sessionDate = String.format("%04d%02d", currentDate.getYear(), currentDate.getMonthValue());
        studentCourse.setCourseSession(sessionDate);
        course.setStartDate(courseStartDate);
        course.setCompletionEndDate(courseCompletionDate);

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("course");
        assertThat(result.get(0).getValidationIssueMessage()).contains("Course session is before the course start date");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.WARNING.getCode());
    }

    @Test
    public void testExecuteValidation_WhenSessionDateIsAfterCourseCompletionDate_ShouldReturnSessionEndError() {
        // Given
        LocalDate currentDate = LocalDate.now();
        LocalDate courseStartDate = currentDate.minusMonths(3);
        LocalDate courseCompletionDate = currentDate.minusMonths(1);
        String sessionDate = YearMonth.from(courseCompletionDate).plusMonths(1).format(COURSE_SESSION_FMT);
        studentCourse.setCourseSession(sessionDate);
        course.setStartDate(courseStartDate);
        course.setCompletionEndDate(courseCompletionDate);

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("course");
        assertThat(result.get(0).getValidationIssueMessage()).contains("Course session is after the course completion date");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.ERROR.getCode());
    }

    @Test
    public void testExecuteValidation_WhenSessionDateIsAfterCurrentReportingPeriod_ShouldReturnSessionValidWarning() {
        // Given
        // Set session to a future date that would be after current reporting period
        LocalDate futureDate = LocalDate.now().plusYears(2);
        String sessionDate = String.format("%04d%02d", futureDate.getYear(), futureDate.getMonthValue());
        studentCourse.setCourseSession(sessionDate);
        // Set course dates to accommodate the future session date
        course.setStartDate(LocalDate.now().minusMonths(1));
        course.setCompletionEndDate(futureDate.plusMonths(1));

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("course");
        assertThat(result.get(0).getValidationIssueMessage()).contains("Course session cannot be after the current reporting period");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.WARNING.getCode());
    }

    @Test
    public void testExecuteValidation_WhenSessionDateIsBeforeMinimumDate_ShouldReturnSessionValidWarning() {
        // Given
        studentCourse.setCourseSession("198301"); // January 1983, before minimum date (1984-01-01)
        // Set course dates to accommodate the historical session date
        course.setStartDate(LocalDate.of(1980, 1, 1));
        course.setCompletionEndDate(LocalDate.of(1985, 12, 31));

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("course");
        assertThat(result.get(0).getValidationIssueMessage()).contains("Course session cannot be after the current reporting period or prior to 198401");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.WARNING.getCode());
    }

    @Test
    public void testExecuteValidation_WhenCourseCodeStartsWithQ_ShouldReturnQCodeWarning() {
        // Given
        course.setCourseCode("QMATH10");

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("course");
        assertThat(result.get(0).getValidationIssueMessage()).contains("Only use Q code if student was on Adult program");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.WARNING.getCode());
    }

    @Test
    public void testExecuteValidation_WhenMultipleIssuesExist_ShouldReturnMultipleValidationIssues() {
        // Given
        LocalDate currentDate = LocalDate.now();
        LocalDate courseStartDate = currentDate.minusMonths(3);
        LocalDate courseCompletionDate = currentDate.minusMonths(1);
        String sessionDate = YearMonth.from(courseCompletionDate).plusMonths(1).format(COURSE_SESSION_FMT);
        studentCourse.setCourseSession(sessionDate); // After completion date
        course.setCourseCode("QMATH10"); // Q code
        course.setStartDate(courseStartDate);
        course.setCompletionEndDate(courseCompletionDate);

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(2);
        
        // Check that both issues are present
        boolean hasSessionEndIssue = result.stream()
                .anyMatch(issue -> issue.getValidationIssueMessage().contains("Course session is after the course completion date"));
        boolean hasQCodeIssue = result.stream()
                .anyMatch(issue -> issue.getValidationIssueMessage().contains("Only use Q code if student was on Adult program"));
        
        assertTrue(hasSessionEndIssue);
        assertTrue(hasQCodeIssue);
    }

    @Test
    public void testExecuteValidation_WhenValidData_ShouldReturnNoIssues() {
        // Given
        LocalDate currentDate = LocalDate.now();
        String sessionDate = String.format("%04d%02d", currentDate.getYear(), currentDate.getMonthValue());
        studentCourse.setCourseSession(sessionDate); // Current month, within valid range
        // Set start date to 1st day of 2 months before (since getSessionDate converts to 1st day)
        course.setStartDate(LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 1).minusMonths(2));
        // Set completion date to 1st day of 2 months after
        course.setCompletionEndDate(LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 1).plusMonths(2));
        course.setCourseCode("MATH10"); // Not a Q code

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void testExecuteValidation_SessionMonthValidation() {
        LocalDate currentDate = LocalDate.now();
        String currentMonth = String.format("%04d%02d", currentDate.getYear(), currentDate.getMonthValue());
        String currentMonthWithDash = String.format("%04d-%02d", currentDate.getYear(), currentDate.getMonthValue());

        Object[][] testCases = {
            // Valid cases - should not have session month error
            {currentMonth, false, "Valid current month"},
            {currentMonthWithDash, false, "Valid current month with dash"},
            {"202303", false, "Valid month 03"},
            {"2023-03", false, "Valid month 03 with dash"},
            
            // Invalid cases - should have session month error
            {"202313", true, "Invalid month 13"},
            {"202300", true, "Invalid month 00"},
            {"2023", true, "Invalid format - too short"},
            {"2023012", true, "Invalid format - too long"}
        };

        for (Object[] testCase : testCases) {
            String sessionValue = (String) testCase[0];
            boolean shouldHaveError = (Boolean) testCase[1];
            String description = (String) testCase[2];

            // Given
            studentCourse.setCourseSession(sessionValue);
            
            // Set up course dates for valid session tests
            if (!shouldHaveError) {
                course.setStartDate(LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 1).minusMonths(2));
                course.setCompletionEndDate(LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 1).plusMonths(2));
            }

            // When
            List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

            // Then
            if (shouldHaveError) {
                assertThat(result).as("Test case: " + description).hasSize(1);
                assertThat(result.get(0).getValidationFieldName()).as("Test case: " + description).isEqualTo("course");
                assertThat(result.get(0).getValidationIssueMessage()).as("Test case: " + description).contains("Course session month must be between 01 and 12");
                assertThat(result.get(0).getValidationIssueSeverityCode()).as("Test case: " + description).isEqualTo(ValidationIssueSeverityCode.ERROR.getCode());
            } else {
                boolean hasSessionMonthIssue = result.stream()
                        .anyMatch(issue -> issue.getValidationIssueMessage().contains("Course session month must be between 01 and 12"));
                assertFalse("Test case: " + description, hasSessionMonthIssue);
            }
        }
    }

    @Test
    public void testExecuteValidation_WithBlankSession_ShouldReturnInvalidDataError() {
        // Given
        studentCourse.setCourseSession(""); // Blank session

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("course");
        assertThat(result.get(0).getValidationIssueMessage()).contains("course id and session cannot be empty");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.ERROR.getCode());
    }

    @Test
    public void testExecuteValidation_WithNullSession_ShouldReturnInvalidDataError() {
        // Given
        studentCourse.setCourseSession(null); // Null session

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getValidationFieldName()).isEqualTo("course");
        assertThat(result.get(0).getValidationIssueMessage()).contains("course id and session cannot be empty");
        assertThat(result.get(0).getValidationIssueSeverityCode()).isEqualTo(ValidationIssueSeverityCode.ERROR.getCode());
    }

    @Test
    public void testExecuteValidation_WhenSessionDateIsExactlyAtCourseStartDate_ShouldNotReturnStartDateWarning() {
        // Given
        LocalDate currentDate = LocalDate.now();
        String sessionDate = String.format("%04d%02d", currentDate.getYear(), currentDate.getMonthValue());
        studentCourse.setCourseSession(sessionDate);
        // Set start date to the 1st day of the same month (since getSessionDate converts to 1st day)
        course.setStartDate(LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 1));
        course.setCompletionEndDate(currentDate.plusMonths(6)); // Set completion date well after session

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void testExecuteValidation_WhenSessionDateIsExactlyAtCourseCompletionDate_ShouldNotReturnCompletionDateError() {
        // Given
        LocalDate currentDate = LocalDate.now();
        String sessionDate = String.format("%04d%02d", currentDate.getYear(), currentDate.getMonthValue());
        studentCourse.setCourseSession(sessionDate);
        course.setStartDate(LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 1).minusMonths(6)); // Set start date well before session
        // Set completion date to the 2nd day of the same month (since validation adds 1 day to session date)
        course.setCompletionEndDate(LocalDate.of(currentDate.getYear(), currentDate.getMonthValue(), 2));

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    public void testExecuteValidation_WhenCourseStartDateIsNull_ShouldNotCheckStartDate() {
        // Given
        LocalDate currentDate = LocalDate.now();
        String sessionDate = String.format("%04d%02d", currentDate.getYear(), currentDate.getMonthValue() - 1);
        studentCourse.setCourseSession(sessionDate); // Previous month
        course.setStartDate(null);

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        // Should not have start date validation issue
        boolean hasStartDateIssue = result.stream()
                .anyMatch(issue -> issue.getValidationIssueMessage().contains("Course session is before the course start date"));
        assertFalse(hasStartDateIssue);
    }

    @Test
    public void testExecuteValidation_WhenCourseCompletionDateIsNull_ShouldNotCheckCompletionDate() {
        // Given
        LocalDate currentDate = LocalDate.now();
        String sessionDate = String.format("%04d%02d", currentDate.getYear(), currentDate.getMonthValue() + 1);
        studentCourse.setCourseSession(sessionDate); // Next month
        course.setCompletionEndDate(null);

        // When
        List<ValidationIssue> result = studentCourseRule.executeValidation(studentCourseRuleData);

        // Then
        // Should not have completion date validation issue
        boolean hasCompletionDateIssue = result.stream()
                .anyMatch(issue -> issue.getValidationIssueMessage().contains("Course session is after the course completion date"));
        assertFalse(hasCompletionDateIssue);
    }
}
