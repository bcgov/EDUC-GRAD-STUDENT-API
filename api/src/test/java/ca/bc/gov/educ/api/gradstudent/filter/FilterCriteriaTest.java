package ca.bc.gov.educ.api.gradstudent.filter;

import ca.bc.gov.educ.api.gradstudent.model.dto.FilterOperation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class FilterCriteriaTest {

    private Function<String, Date> dateConverter;

    @Before
    public void setUp() {
        dateConverter = s -> {
            if (s == null || s.trim().isEmpty()) return null;
            s = s.trim();
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };
    }

    @Test
    public void testDateRangeOperation_BothDates() {
        String fieldName = "programCompletionDate";
        String fieldValue = "2025-12-11,2026-01-01";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        FilterCriteria<Date> criteria = new FilterCriteria<>(fieldName, fieldValue, operation, dateConverter);

        assertNotNull("Min value should not be null", criteria.getMinValue());
        assertNotNull("Max value should not be null", criteria.getMaxValue());
        assertEquals("Field name should match", fieldName, criteria.getFieldName());
        assertEquals("Operation should be DATE_RANGE", FilterOperation.DATE_RANGE, criteria.getOperation());
    }

    @Test
    public void testDateRangeOperation_OnlyStartDate() {
        String fieldValue = "2025-12-11,";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        FilterCriteria<Date> criteria = new FilterCriteria<>("fieldName", fieldValue, operation, dateConverter);

        assertNotNull("Min value should not be null", criteria.getMinValue());
        assertNull("Max value should be null", criteria.getMaxValue());
    }

    @Test
    public void testDateRangeOperation_OnlyEndDate() {
        String fieldValue = ",2026-01-01";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        FilterCriteria<Date> criteria = new FilterCriteria<>("fieldName", fieldValue, operation, dateConverter);

        assertNull("Min value should be null", criteria.getMinValue());
        assertNotNull("Max value should not be null", criteria.getMaxValue());
    }

    @Test
    public void testDateRangeOperation_WithDateTime() {
        String fieldValue = "2025-12-11T00:00:00,2026-01-01T23:59:59";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        FilterCriteria<Date> criteria = new FilterCriteria<>("fieldName", fieldValue, operation, dateConverter);

        assertNotNull("Min value should not be null", criteria.getMinValue());
        assertNotNull("Max value should not be null", criteria.getMaxValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDateRangeOperation_BothEmpty() {
        String fieldValue = ",";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        new FilterCriteria<>("fieldName", fieldValue, operation, dateConverter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDateRangeOperation_StartGreaterThanEnd() {
        String fieldValue = "2026-01-01,2025-12-11";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        new FilterCriteria<>("fieldName", fieldValue, operation, dateConverter);
    }

    @Test
    public void testDateRangeOperation_SameDates() {
        String fieldValue = "2025-12-11,2025-12-11";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        FilterCriteria<Date> criteria = new FilterCriteria<>("fieldName", fieldValue, operation, dateConverter);

        assertNotNull("Min value should not be null", criteria.getMinValue());
        assertNotNull("Max value should not be null", criteria.getMaxValue());
        assertEquals("Min and max should be equal", criteria.getMinValue(), criteria.getMaxValue());
    }

    @Test
    public void testBetweenOperation() {
        String fieldValue = "100,200";
        FilterOperation operation = FilterOperation.BETWEEN;
        Function<String, Integer> intConverter = Integer::valueOf;

        FilterCriteria<Integer> criteria = new FilterCriteria<>("fieldName", fieldValue, operation, intConverter);

        assertNotNull("Min value should not be null", criteria.getMinValue());
        assertNotNull("Max value should not be null", criteria.getMaxValue());
        assertEquals(Integer.valueOf(100), criteria.getMinValue());
        assertEquals(Integer.valueOf(200), criteria.getMaxValue());
    }

    @Test
    public void testEqualOperation() {
        String fieldValue = "test";
        FilterOperation operation = FilterOperation.EQUAL;
        Function<String, String> stringConverter = s -> s;

        FilterCriteria<String> criteria = new FilterCriteria<>("fieldName", fieldValue, operation, stringConverter);

        assertEquals("test", criteria.getConvertedSingleValue());
    }

    @Test
    public void testInOperation() {
        String fieldValue = "value1,value2,value3";
        FilterOperation operation = FilterOperation.IN;
        Function<String, String> stringConverter = s -> s;

        FilterCriteria<String> criteria = new FilterCriteria<>("fieldName", fieldValue, operation, stringConverter);

        assertNotNull("Converted values should not be null", criteria.getConvertedValues());
        assertEquals("Should have 3 values", 3, criteria.getConvertedValues().size());
    }

    @Test
    public void testDateRangePreservesEmptyStrings() {
        String fieldValueStartOnly = "2025-12-11,";
        FilterCriteria<Date> criteriaStartOnly = new FilterCriteria<>("fieldName", fieldValueStartOnly,
            FilterOperation.DATE_RANGE, dateConverter);

        assertNotNull("Start date should be present", criteriaStartOnly.getMinValue());
        assertNull("End date should be null", criteriaStartOnly.getMaxValue());

        String fieldValueEndOnly = ",2026-01-01";
        FilterCriteria<Date> criteriaEndOnly = new FilterCriteria<>("fieldName", fieldValueEndOnly,
            FilterOperation.DATE_RANGE, dateConverter);

        assertNull("Start date should be null", criteriaEndOnly.getMinValue());
        assertNotNull("End date should be present", criteriaEndOnly.getMaxValue());
    }

    @Test
    public void testDateRangeWithWhitespace() {
        String fieldValue = " 2025-12-11 , 2026-01-01 ";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        FilterCriteria<Date> criteria = new FilterCriteria<>("fieldName", fieldValue, operation, dateConverter);

        assertNotNull("Min value should not be null", criteria.getMinValue());
        assertNotNull("Max value should not be null", criteria.getMaxValue());
    }

    @Test
    public void testDateRangeOriginalValuesPreserved() {
        String fieldValue = "2025-12-11,2026-01-01";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        FilterCriteria<Date> criteria = new FilterCriteria<>("fieldName", fieldValue, operation, dateConverter);

        assertNotNull("Original values should be preserved", criteria.getOriginalValues());
        assertEquals("Should have 2 original values", 2, criteria.getOriginalValues().size());
    }

    @Test
    public void testDateRangeDateOrdering() {
        String fieldValue = "2025-12-11,2026-06-30";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        FilterCriteria<Date> criteria = new FilterCriteria<>("fieldName", fieldValue, operation, dateConverter);

        assertTrue("Min should be before Max",
            criteria.getMinValue().before(criteria.getMaxValue()));
    }

    @Test
    public void testGetFieldName() {
        String fieldName = "testField";
        FilterCriteria<Date> criteria = new FilterCriteria<>(fieldName, "2025-12-11,2026-01-01",
            FilterOperation.DATE_RANGE, dateConverter);

        assertEquals("Field name should match", fieldName, criteria.getFieldName());
    }

    @Test
    public void testGetOperation() {
        FilterOperation operation = FilterOperation.DATE_RANGE;
        FilterCriteria<Date> criteria = new FilterCriteria<>("fieldName", "2025-12-11,2026-01-01",
            operation, dateConverter);

        assertEquals("Operation should match", operation, criteria.getOperation());
    }

    @Test
    public void testGetConverterFunction() {
        FilterCriteria<Date> criteria = new FilterCriteria<>("fieldName", "2025-12-11,2026-01-01",
            FilterOperation.DATE_RANGE, dateConverter);

        assertNotNull("Converter function should not be null", criteria.getConverterFunction());
        assertEquals("Converter function should match", dateConverter, criteria.getConverterFunction());
    }

    @Test
    public void testGetOriginalValues() {
        FilterCriteria<Date> criteria = new FilterCriteria<>("fieldName", "2025-12-11,2026-01-01",
            FilterOperation.DATE_RANGE, dateConverter);

        assertNotNull("Original values should not be null", criteria.getOriginalValues());
        assertTrue("Original values should contain expected values",
            criteria.getOriginalValues().contains("2025-12-11"));
    }

    @Test
    public void testGetConvertedValues() {
        FilterCriteria<String> criteria = new FilterCriteria<>("fieldName", "value1,value2",
            FilterOperation.IN, s -> s);

        assertNotNull("Converted values should not be null", criteria.getConvertedValues());
        assertEquals("Should have 2 converted values", 2, criteria.getConvertedValues().size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBetweenOperation_InvalidValueCount() {
        String fieldValue = "100";
        FilterOperation operation = FilterOperation.BETWEEN;
        Function<String, Integer> intConverter = Integer::valueOf;

        new FilterCriteria<>("fieldName", fieldValue, operation, intConverter);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDateRangeOperation_InvalidValueCount() {
        String fieldValue = "2025-12-11";
        FilterOperation operation = FilterOperation.DATE_RANGE;

        new FilterCriteria<>("fieldName", fieldValue, operation, dateConverter);
    }
}

