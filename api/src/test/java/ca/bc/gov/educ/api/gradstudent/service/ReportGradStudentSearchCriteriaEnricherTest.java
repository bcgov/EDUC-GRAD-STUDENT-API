package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.Condition;
import ca.bc.gov.educ.api.gradstudent.model.dto.FilterOperation;
import ca.bc.gov.educ.api.gradstudent.model.dto.Search;
import ca.bc.gov.educ.api.gradstudent.model.dto.SearchCriteria;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValueType;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ReportGradStudentSearchCriteriaEnricherTest {
  @Mock
  private RestUtils restUtils;

  @InjectMocks
  private ReportGradStudentSearchCriteriaEnricher enricher;

  @Test
  public void testEnrich_WithDistrictAndSchoolCategory_RewritesToSchoolOfRecordIdIn() {
    final String districtId = UUID.randomUUID().toString();
    final String matchingSchoolId = UUID.randomUUID().toString();
    final String nonMatchingSchoolId = UUID.randomUUID().toString();

    when(restUtils.getSchoolList()).thenReturn(List.of(
      School.builder().schoolId(matchingSchoolId).districtId(districtId).schoolCategoryCode("PUBLIC").build(),
      School.builder().schoolId(nonMatchingSchoolId).districtId(districtId).schoolCategoryCode("INDEPEND").build()
    ));

    final Search search = Search.builder()
      .searchCriteriaList(List.of(
        SearchCriteria.builder().key("districtId").operation(FilterOperation.EQUAL).value(districtId).valueType(ValueType.UUID).build(),
        SearchCriteria.builder().key("schoolCategoryCode").operation(FilterOperation.EQUAL).value("PUBLIC").valueType(ValueType.STRING).condition(Condition.AND).build(),
        SearchCriteria.builder().key("status").operation(FilterOperation.EQUAL).value("CUR").valueType(ValueType.STRING).condition(Condition.AND).build()
      ))
      .build();

    final List<Search> result = enricher.enrich(List.of(search));

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals(2, result.get(0).getSearchCriteriaList().size());

    final SearchCriteria rewrittenCriterion = result.get(0).getSearchCriteriaList().get(0);
    assertEquals("schoolOfRecordId", rewrittenCriterion.getKey());
    assertEquals(FilterOperation.IN, rewrittenCriterion.getOperation());
    assertEquals(matchingSchoolId, rewrittenCriterion.getValue());
    assertEquals(ValueType.UUID, rewrittenCriterion.getValueType());

    final SearchCriteria statusCriterion = result.get(0).getSearchCriteriaList().get(1);
    assertEquals("status", statusCriterion.getKey());
    assertEquals("CUR", statusCriterion.getValue());
    assertEquals(Condition.AND, statusCriterion.getCondition());
  }

  @Test
  public void testEnrich_WithNoMatchingSchools_UsesSentinelSchoolId() {
    final String districtId = UUID.randomUUID().toString();

    when(restUtils.getSchoolList()).thenReturn(List.of(
      School.builder().schoolId(UUID.randomUUID().toString()).districtId(UUID.randomUUID().toString()).schoolCategoryCode("PUBLIC").build()
    ));

    final Search search = Search.builder()
      .searchCriteriaList(List.of(
        SearchCriteria.builder().key("districtId").operation(FilterOperation.EQUAL).value(districtId).valueType(ValueType.UUID).build()
      ))
      .build();

    final List<Search> result = enricher.enrich(List.of(search));

    assertEquals("00000000-0000-0000-0000-000000000000", result.get(0).getSearchCriteriaList().get(0).getValue());
  }
}
