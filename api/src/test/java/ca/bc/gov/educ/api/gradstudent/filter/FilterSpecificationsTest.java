package ca.bc.gov.educ.api.gradstudent.filter;

import ca.bc.gov.educ.api.gradstudent.model.dto.FilterOperation;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCoursePaginationEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Function;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class FilterSpecificationsTest {

    private FilterSpecifications<StudentCoursePaginationEntity, Date> filterSpecifications;

    @Before
    public void setUp() {
        filterSpecifications = new FilterSpecifications<>();
    }

    private Date createDate(String dateStr) {
        LocalDate localDate = LocalDate.parse(dateStr);
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    @Test
    public void testDateRangeOperation_DirectProperty_BothDates() {
        Function<String, Date> converter = s -> {
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };

        FilterCriteria<Date> criteria = new FilterCriteria<>("completionDate", "2025-01-01,2025-12-31",
            FilterOperation.DATE_RANGE, converter);

        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        assertNotNull("DATE_RANGE specification should exist", specFunction);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);

        assertNotNull("Min value should be set", criteria.getMinValue());
        assertNotNull("Max value should be set", criteria.getMaxValue());
    }

    @Test
    public void testDateRangeOperation_DirectProperty_OnlyStartDate() {
        Function<String, Date> converter = s -> {
            if (s == null || s.trim().isEmpty()) return null;
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };

        FilterCriteria<Date> criteria = new FilterCriteria<>("completionDate", "2025-01-01,",
            FilterOperation.DATE_RANGE, converter);

        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);

        assertNotNull("Min value should be set", criteria.getMinValue());
        assertNull("Max value should be null", criteria.getMaxValue());
    }

    @Test
    public void testDateRangeOperation_DirectProperty_OnlyEndDate() {
        Function<String, Date> converter = s -> {
            if (s == null || s.trim().isEmpty()) return null;
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };

        FilterCriteria<Date> criteria = new FilterCriteria<>("completionDate", ",2025-12-31",
            FilterOperation.DATE_RANGE, converter);

        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);

        assertNull("Min value should be null", criteria.getMinValue());
        assertNotNull("Max value should be set", criteria.getMaxValue());
    }

    @Test
    public void testDateRangeOperation_NestedProperty_BothDates() {
        Function<String, Date> converter = s -> {
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };

        FilterCriteria<Date> criteria = new FilterCriteria<>("graduationStudentRecordEntity.programCompletionDate",
            "2025-01-01,2025-12-31", FilterOperation.DATE_RANGE, converter);

        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);

        assertTrue("Field name should contain dot", criteria.getFieldName().contains("."));
        assertNotNull("Min value should be set", criteria.getMinValue());
        assertNotNull("Max value should be set", criteria.getMaxValue());
    }

    @Test
    public void testDateRangeOperation_NestedProperty_OnlyStartDate() {
        Function<String, Date> converter = s -> {
            if (s == null || s.trim().isEmpty()) return null;
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };

        FilterCriteria<Date> criteria = new FilterCriteria<>("graduationStudentRecordEntity.programCompletionDate",
            "2025-01-01,", FilterOperation.DATE_RANGE, converter);

        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);

        assertNotNull("Min value should be set", criteria.getMinValue());
        assertNull("Max value should be null", criteria.getMaxValue());
    }

    @Test
    public void testDateRangeOperation_NestedProperty_OnlyEndDate() {
        Function<String, Date> converter = s -> {
            if (s == null || s.trim().isEmpty()) return null;
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };

        FilterCriteria<Date> criteria = new FilterCriteria<>("graduationStudentRecordEntity.programCompletionDate",
            ",2025-12-31", FilterOperation.DATE_RANGE, converter);

        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);

        assertNull("Min value should be null", criteria.getMinValue());
        assertNotNull("Max value should be set", criteria.getMaxValue());
    }

    @Test
    public void testDateRangeOperation_ThreeLevelNested_BothDates() {
        Function<String, Date> converter = s -> {
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };

        FilterCriteria<Date> criteria = new FilterCriteria<>("entity.subEntity.completionDate",
            "2025-01-01,2025-12-31", FilterOperation.DATE_RANGE, converter);

        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);

        long dotCount = criteria.getFieldName().chars().filter(ch -> ch == '.').count();
        assertEquals("Should have 2 dots for 3 levels", 2, dotCount);
    }

    @Test
    public void testDateRangeOperation_ThreeLevelNested_OnlyStartDate() {
        Function<String, Date> converter = s -> {
            if (s == null || s.trim().isEmpty()) return null;
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };

        FilterCriteria<Date> criteria = new FilterCriteria<>("entity.subEntity.completionDate",
            "2025-01-01,", FilterOperation.DATE_RANGE, converter);

        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);

        assertNotNull("Min value should be set", criteria.getMinValue());
        assertNull("Max value should be null", criteria.getMaxValue());
    }

    @Test
    public void testDateRangeOperation_ThreeLevelNested_OnlyEndDate() {
        Function<String, Date> converter = s -> {
            if (s == null || s.trim().isEmpty()) return null;
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };

        FilterCriteria<Date> criteria = new FilterCriteria<>("entity.subEntity.completionDate",
            ",2025-12-31", FilterOperation.DATE_RANGE, converter);

        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);

        assertNull("Min value should be null", criteria.getMinValue());
        assertNotNull("Max value should be set", criteria.getMaxValue());
    }

    @Test
    public void testAllFilterOperationsHaveSpecifications() {
        FilterOperation[] operations = {
            FilterOperation.EQUAL,
            FilterOperation.NOT_EQUAL,
            FilterOperation.GREATER_THAN,
            FilterOperation.GREATER_THAN_OR_EQUAL_TO,
            FilterOperation.LESS_THAN,
            FilterOperation.LESS_THAN_OR_EQUAL_TO,
            FilterOperation.IN,
            FilterOperation.NOT_IN,
            FilterOperation.BETWEEN,
            FilterOperation.CONTAINS,
            FilterOperation.DATE_RANGE
        };

        for (FilterOperation operation : operations) {
            Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> spec =
                filterSpecifications.getSpecification(operation);
            assertNotNull("Specification should exist for operation: " + operation, spec);
        }
    }

    @Test
    public void testDateRangeSpecificationReturnedCorrectly() {
        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        assertNotNull("DATE_RANGE specification function should not be null", specFunction);

        Function<String, Date> converter = s -> createDate("2025-01-01");
        FilterCriteria<Date> criteria = new FilterCriteria<>("testField", "2025-01-01,2025-12-31",
            FilterOperation.DATE_RANGE, converter);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);
    }

    @Test
    public void testDateRangeWithDateTimeStrings() {
        Function<String, Date> converter = s -> {
            if (s == null || s.trim().isEmpty()) return null;
            String dateString = s.contains("T") ? s.split("T")[0] : s;
            LocalDate localDate = LocalDate.parse(dateString);
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        };

        FilterCriteria<Date> criteria = new FilterCriteria<>("completionDate",
            "2025-01-01T00:00:00,2025-12-31T23:59:59",
            FilterOperation.DATE_RANGE, converter);

        Function<FilterCriteria<Date>, Specification<StudentCoursePaginationEntity>> specFunction =
            filterSpecifications.getSpecification(FilterOperation.DATE_RANGE);

        Specification<StudentCoursePaginationEntity> spec = specFunction.apply(criteria);
        assertNotNull("Specification should not be null", spec);

        assertNotNull("Min value should be set", criteria.getMinValue());
        assertNotNull("Max value should be set", criteria.getMaxValue());
    }
}

