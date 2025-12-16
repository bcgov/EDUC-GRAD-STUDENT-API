package ca.bc.gov.educ.api.gradstudent.model.mapper;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DateMapperTest {

    private final DateMapper dateMapper = new DateMapper();
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    @Test
    public void testMap_dateToString_withValidDate_returnsFormattedString() throws Exception {
        // Given
        Date date = DATE_TIME_FORMAT.parse("2025-12-16T10:30:45");

        // When
        String result = dateMapper.map(date);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("2025-12-16T10:30:45");
    }

    @Test
    public void testMap_dateToString_withNullDate_returnsNull() {
        // Given
        Date date = null;

        // When
        String result = dateMapper.map(date);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testMap_dateToString_withEpochDate_returnsFormattedString() {
        // Given
        Date date = new Date(0); // 1970-01-01T00:00:00 UTC

        // When
        String result = dateMapper.map(date);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
    }

    @Test
    public void testMap_stringToDate_withValidString_returnsDate() throws Exception {
        // Given
        String dateStr = "2025-12-16T10:30:45";
        Date expectedDate = DATE_TIME_FORMAT.parse(dateStr);

        // When
        Date result = dateMapper.map(dateStr);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDate);
    }

    @Test
    public void testMap_stringToDate_withNullString_returnsNull() {
        // Given
        String dateStr = null;

        // When
        Date result = dateMapper.map(dateStr);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testMap_stringToDate_withInvalidString_returnsNull() {
        // Given
        String dateStr = "invalid-date-string";

        // When
        Date result = dateMapper.map(dateStr);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testMap_stringToDate_withEmptyString_returnsNull() {
        // Given
        String dateStr = "";

        // When
        Date result = dateMapper.map(dateStr);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testMap_stringToDate_withWrongFormat_returnsNull() {
        // Given
        String dateStr = "16/12/2025"; // Wrong format

        // When
        Date result = dateMapper.map(dateStr);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testMap_stringToDate_withPartialDate_returnsNull() {
        // Given
        String dateStr = "2025-12-16"; // Missing time portion

        // When
        Date result = dateMapper.map(dateStr);

        // Then
        assertThat(result).isNull();
    }

    @Test
    public void testMap_roundTrip_dateToStringToDate_preservesValue() throws Exception {
        // Given
        Date originalDate = DATE_TIME_FORMAT.parse("2025-12-16T14:25:30");

        // When
        String dateString = dateMapper.map(originalDate);
        Date resultDate = dateMapper.map(dateString);

        // Then
        assertThat(resultDate).isNotNull();
        assertThat(resultDate).isEqualTo(originalDate);
    }

    @Test
    public void testMap_dateToString_withLeapYearDate_returnsFormattedString() throws Exception {
        // Given
        Date date = DATE_TIME_FORMAT.parse("2024-02-29T23:59:59"); // Leap year

        // When
        String result = dateMapper.map(date);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo("2024-02-29T23:59:59");
    }

    @Test
    public void testMap_stringToDate_withMidnightTime_returnsDate() throws Exception {
        // Given
        String dateStr = "2025-01-01T00:00:00";
        Date expectedDate = DATE_TIME_FORMAT.parse(dateStr);

        // When
        Date result = dateMapper.map(dateStr);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDate);
    }

    @Test
    public void testMap_stringToDate_withEndOfDayTime_returnsDate() throws Exception {
        // Given
        String dateStr = "2025-12-31T23:59:59";
        Date expectedDate = DATE_TIME_FORMAT.parse(dateStr);

        // When
        Date result = dateMapper.map(dateStr);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(expectedDate);
    }
}

