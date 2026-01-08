package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.filter.StudentOptionalProgramPaginationFilterSpecs;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.repository.StudentOptionalProgramPaginationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StudentOptionalProgramPaginationServiceTest {

    @Mock
    private StudentOptionalProgramPaginationRepository studentOptionalProgramPaginationRepository;

    @Mock
    private StudentOptionalProgramPaginationFilterSpecs studentOptionalProgramPaginationFilterSpecs;

    @InjectMocks
    private StudentOptionalProgramPaginationService studentOptionalProgramPaginationService;

    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testFindAll_Success() throws Exception {
        // Arrange
        List<StudentOptionalProgramPaginationEntity> entities = new ArrayList<>();
        StudentOptionalProgramPaginationEntity entity = new StudentOptionalProgramPaginationEntity();
        entities.add(entity);

        Page<StudentOptionalProgramPaginationEntity> page = new PageImpl<>(entities);
        when(studentOptionalProgramPaginationRepository.findAll(
                ArgumentMatchers.nullable(Specification.class),
                any(PageRequest.class)))
                .thenReturn(page);

        List<Sort.Order> sorts = new ArrayList<>();
        sorts.add(new Sort.Order(Sort.Direction.ASC, "studentOptionalProgramID"));

        // Act
        CompletableFuture<Page<StudentOptionalProgramPaginationEntity>> result =
                studentOptionalProgramPaginationService.findAll(null, 0, 10, sorts);

        // Assert
        assertNotNull(result);
        Page<StudentOptionalProgramPaginationEntity> resultPage = result.get();
        assertNotNull(resultPage);
        assertEquals(1, resultPage.getTotalElements());
    }

    @Test
    public void testSetSpecificationAndSortCriteria_NoSearchCriteria() {
        // Arrange
        String sortCriteriaJson = "";
        String searchCriteriaListJson = null;
        List<Sort.Order> sorts = new ArrayList<>();

        // Act
        Specification<StudentOptionalProgramPaginationEntity> result =
                studentOptionalProgramPaginationService.setSpecificationAndSortCriteria(
                        sortCriteriaJson, searchCriteriaListJson, objectMapper, sorts);

        // Assert
        assertNull(result);
    }

    @Test
    public void testSetSpecificationAndSortCriteria_WithSearchCriteria() {
        // Arrange
        String sortCriteriaJson = "";
        String searchCriteriaListJson = "[{\"searchCriteriaList\":[{\"key\":\"optionalProgramID\",\"operation\":\"eq\",\"value\":\"123e4567-e89b-12d3-a456-426614174000\",\"valueType\":\"UUID\",\"condition\":\"AND\"}]}]";
        List<Sort.Order> sorts = new ArrayList<>();

        // Act
        Specification<StudentOptionalProgramPaginationEntity> result =
                studentOptionalProgramPaginationService.setSpecificationAndSortCriteria(
                        sortCriteriaJson, searchCriteriaListJson, objectMapper, sorts);

        // Assert
        assertNotNull(result);
    }

    @Test
    public void testGetStudentOptionalProgramPaginationFilterSpecs() {
        // Act
        StudentOptionalProgramPaginationFilterSpecs result =
                studentOptionalProgramPaginationService.getStudentOptionalProgramPaginationFilterSpecs();

        // Assert
        assertNotNull(result);
        assertEquals(studentOptionalProgramPaginationFilterSpecs, result);
    }
}

