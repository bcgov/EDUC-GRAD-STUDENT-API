package ca.bc.gov.educ.api.gradstudent.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class EducGradStudentApiUtilsTest {

    private static final SimpleDateFormat PROGRAM_COMPLETION_DATE_FORMAT = new SimpleDateFormat("yyyy/MM");

    @Test
    public void testIsDateInFuture_whenProgramCompletionDateIsInFutureYear_thenReturnsTrue() throws Exception {
        Date programCompletionDate = PROGRAM_COMPLETION_DATE_FORMAT.parse("2099/01");

        boolean result = EducGradStudentApiUtils.isDateInFuture(programCompletionDate);

        assertThat(result).isTrue();
    }

    @Test
    public void testIsDateInFuture_whenProgramCompletionDateIsInPast_thenReturnsFalse() throws Exception {
        Date programCompletionDate = PROGRAM_COMPLETION_DATE_FORMAT.parse("2023/01");

        boolean result = EducGradStudentApiUtils.isDateInFuture(programCompletionDate);

        assertThat(result).isFalse();
    }

    @Test
    public void testParsingProgramCompletionDate_whenDashFormat_thenReturnsMonthEnd() {
        Date result = EducGradStudentApiUtils.parsingProgramCompletionDate("2026-04");

        assertThat(EducGradStudentApiUtils.getProgramCompletionDate(result)).isEqualTo("2026-04-30");
    }

    @Test
    public void testParsingProgramCompletionDate_whenSlashFormat_thenReturnsMonthEnd() {
        Date result = EducGradStudentApiUtils.parsingProgramCompletionDate("2026/04");

        assertThat(EducGradStudentApiUtils.getProgramCompletionDate(result)).isEqualTo("2026-04-30");
    }
}
