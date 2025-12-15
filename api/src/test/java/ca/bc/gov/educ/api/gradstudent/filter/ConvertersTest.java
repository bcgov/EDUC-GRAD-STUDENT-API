package ca.bc.gov.educ.api.gradstudent.filter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class ConvertersTest {

    private Converters converters;

    @Before
    public void setUp() {
        converters = new Converters();
        converters.init();
    }

    @Test
    public void testStringConverter() {
        Function<String, String> converter = converters.getFunction(String.class);
        assertNotNull("String converter should not be null", converter);

        String result = converter.apply("test");
        assertEquals("test", result);
    }

    @Test
    public void testLongConverter() {
        Function<String, Long> converter = converters.getFunction(Long.class);
        assertNotNull("Long converter should not be null", converter);

        Long result = converter.apply("12345");
        assertEquals(Long.valueOf(12345), result);
    }

    @Test
    public void testIntegerConverter() {
        Function<String, Integer> converter = converters.getFunction(Integer.class);
        assertNotNull("Integer converter should not be null", converter);

        Integer result = converter.apply("999");
        assertEquals(Integer.valueOf(999), result);
    }

    @Test
    public void testBooleanConverter() {
        Function<String, Boolean> converter = converters.getFunction(Boolean.class);
        assertNotNull("Boolean converter should not be null", converter);

        Boolean resultTrue = converter.apply("true");
        Boolean resultFalse = converter.apply("false");
        assertTrue(resultTrue);
        assertFalse(resultFalse);
    }

    @Test
    public void testUUIDConverter() {
        Function<String, UUID> converter = converters.getFunction(UUID.class);
        assertNotNull("UUID converter should not be null", converter);

        String uuidString = "123e4567-e89b-12d3-a456-426614174000";
        UUID result = converter.apply(uuidString);
        assertEquals(UUID.fromString(uuidString), result);
    }

    @Test
    public void testChronoLocalDateConverter() {
        Function<String, ChronoLocalDate> converter = converters.getFunction(ChronoLocalDate.class);
        assertNotNull("ChronoLocalDate converter should not be null", converter);

        ChronoLocalDate result = converter.apply("2025-12-11");
        assertEquals(LocalDate.of(2025, 12, 11), result);
    }

    @Test
    public void testChronoLocalDateTimeConverter_WithDateTime() {
        Function<String, ChronoLocalDateTime<?>> converter = converters.getFunction(ChronoLocalDateTime.class);
        assertNotNull("ChronoLocalDateTime converter should not be null", converter);

        ChronoLocalDateTime<?> result = converter.apply("2025-12-11T10:30:00");
        assertEquals(LocalDateTime.of(2025, 12, 11, 10, 30, 0), result);
    }

    @Test
    public void testUtilDateConverter_WithDateOnly() {
        Function<String, Date> converter = converters.getFunction(Date.class);
        assertNotNull("Date converter should not be null", converter);

        Date result = converter.apply("2025-12-11");
        assertNotNull(result);

        LocalDate expectedDate = LocalDate.of(2025, 12, 11);
        LocalDateTime resultDateTime = LocalDateTime.ofInstant(result.toInstant(),
            java.time.ZoneId.systemDefault());
        assertEquals(expectedDate, resultDateTime.toLocalDate());
        assertEquals(0, resultDateTime.getHour());
        assertEquals(0, resultDateTime.getMinute());
        assertEquals(0, resultDateTime.getSecond());
    }

    @Test
    public void testUtilDateConverter_WithDateTime() {
        Function<String, Date> converter = converters.getFunction(Date.class);
        assertNotNull("Date converter should not be null", converter);

        Date result = converter.apply("2025-12-11T23:59:59");
        assertNotNull(result);

        LocalDate expectedDate = LocalDate.of(2025, 12, 11);
        LocalDateTime resultDateTime = LocalDateTime.ofInstant(result.toInstant(),
            java.time.ZoneId.systemDefault());
        assertEquals(expectedDate, resultDateTime.toLocalDate());
        assertEquals(0, resultDateTime.getHour());
        assertEquals(0, resultDateTime.getMinute());
        assertEquals(0, resultDateTime.getSecond());
    }

    @Test
    public void testUtilDateConverter_MultipleFormats() {
        Function<String, Date> converter = converters.getFunction(Date.class);

        String[] dateStrings = {
            "2025-12-11",
            "2025-12-11T00:00:00",
            "2025-12-11T23:59:59",
            "2025-01-01T12:30:45"
        };

        for (String dateString : dateStrings) {
            Date result = converter.apply(dateString);
            assertNotNull("Date should be converted for: " + dateString, result);

            LocalDateTime resultDateTime = LocalDateTime.ofInstant(result.toInstant(),
                java.time.ZoneId.systemDefault());
            assertEquals("Time should be at start of day for: " + dateString,
                0, resultDateTime.getHour());
        }
    }

    @Test(expected = NumberFormatException.class)
    public void testLongConverter_InvalidInput() {
        Function<String, Long> converter = converters.getFunction(Long.class);
        converter.apply("not-a-number");
    }

    @Test(expected = java.time.format.DateTimeParseException.class)
    public void testChronoLocalDateConverter_InvalidDate() {
        Function<String, ChronoLocalDate> converter = converters.getFunction(ChronoLocalDate.class);
        converter.apply("invalid-date");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUUIDConverter_InvalidUUID() {
        Function<String, UUID> converter = converters.getFunction(UUID.class);
        converter.apply("not-a-uuid");
    }

    @Test
    public void testAllConvertersInitialized() {
        assertNotNull("String converter missing", converters.getFunction(String.class));
        assertNotNull("Long converter missing", converters.getFunction(Long.class));
        assertNotNull("Integer converter missing", converters.getFunction(Integer.class));
        assertNotNull("Boolean converter missing", converters.getFunction(Boolean.class));
        assertNotNull("UUID converter missing", converters.getFunction(UUID.class));
        assertNotNull("ChronoLocalDate converter missing", converters.getFunction(ChronoLocalDate.class));
        assertNotNull("ChronoLocalDateTime converter missing", converters.getFunction(ChronoLocalDateTime.class));
        assertNotNull("Date converter missing", converters.getFunction(Date.class));
    }

    @Test
    public void testDateConverterConsistency() {
        Function<String, Date> converter = converters.getFunction(Date.class);

        Date result1 = converter.apply("2025-12-11");
        Date result2 = converter.apply("2025-12-11");

        assertEquals("Same input should produce same output", result1, result2);
    }

    @Test
    public void testDateConverterTimeStripping() {
        Function<String, Date> converter = converters.getFunction(Date.class);

        Date morning = converter.apply("2025-12-11T08:00:00");
        Date afternoon = converter.apply("2025-12-11T14:30:00");
        Date evening = converter.apply("2025-12-11T23:59:59");

        assertEquals("All times on same date should produce same Date", morning, afternoon);
        assertEquals("All times on same date should produce same Date", afternoon, evening);
    }
}

