package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.model.dto.FilterOperation;
import ca.bc.gov.educ.api.gradstudent.model.dto.Search;
import ca.bc.gov.educ.api.gradstudent.model.dto.SearchCriteria;
import ca.bc.gov.educ.api.gradstudent.model.dto.ValueType;
import ca.bc.gov.educ.api.gradstudent.model.dto.institute.School;
import ca.bc.gov.educ.api.gradstudent.rest.RestUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Enriches the search to include students belonging to districts
 * and deeper searches like under which school category code
 */
@Service
@RequiredArgsConstructor
public class ReportGradStudentSearchCriteriaEnricher {
  private static final String DISTRICT_ID = "districtId";
  private static final String SCHOOL_CATEGORY_CODE = "schoolCategoryCode";
  private static final String SCHOOL_OF_RECORD_ID = "schoolOfRecordId";
  private static final String NO_MATCHING_SCHOOL_ID = "00000000-0000-0000-0000-000000000000";

  private final RestUtils restUtils;

  public List<Search> enrich(final List<Search> searches) {
    return searches.stream().map(this::enrichSearch).toList();
  }

  private Search enrichSearch(final Search search) {
    if (search.getSearchCriteriaList() == null || search.getSearchCriteriaList().isEmpty()) {
      return search;
    }

    final List<SearchCriteria> criteriaList = search.getSearchCriteriaList();
    final List<SearchCriteria> scopedCriteria = criteriaList.stream()
      .filter(this::isScopedCriterion)
      .toList();

    if (scopedCriteria.isEmpty()) {
      return search;
    }

    final Set<String> districtIds = getCriterionValues(scopedCriteria, DISTRICT_ID);
    final Set<String> schoolCategoryCodes = getCriterionValues(scopedCriteria, SCHOOL_CATEGORY_CODE);
    final SearchCriteria replacementCriterion = buildSchoolOfRecordCriterion(
      criteriaList,
      resolveSchoolIds(districtIds, schoolCategoryCodes)
    );

    final List<SearchCriteria> rewrittenCriteria = new ArrayList<>();
    boolean replacementInserted = false;
    for (final SearchCriteria criteria : criteriaList) {
      if (isScopedCriterion(criteria)) {
        if (!replacementInserted) {
          rewrittenCriteria.add(replacementCriterion);
          replacementInserted = true;
        }
      } else {
        rewrittenCriteria.add(criteria);
      }
    }

    return Search.builder()
      .condition(search.getCondition())
      .searchCriteriaList(rewrittenCriteria)
      .build();
  }

  private SearchCriteria buildSchoolOfRecordCriterion(final List<SearchCriteria> criteriaList, final List<String> schoolIds) {
    final SearchCriteria firstScopedCriterion = criteriaList.stream()
      .filter(this::isScopedCriterion)
      .findFirst()
      .orElseThrow();

    return SearchCriteria.builder()
      .key(SCHOOL_OF_RECORD_ID)
      .operation(FilterOperation.IN)
      .value(String.join(",", schoolIds))
      .valueType(ValueType.UUID)
      .condition(firstScopedCriterion.getCondition())
      .build();
  }

  private List<String> resolveSchoolIds(final Set<String> districtIds, final Set<String> schoolCategoryCodes) {
    return restUtils.getSchoolList().stream()
      .filter(school -> districtIds.isEmpty() || districtIds.contains(school.getDistrictId()))
      .filter(school -> schoolCategoryCodes.isEmpty() || containsIgnoreCase(schoolCategoryCodes, school.getSchoolCategoryCode()))
      .map(School::getSchoolId)
      .filter(StringUtils::isNotBlank)
      .distinct()
      .collect(Collectors.collectingAndThen(Collectors.toList(), schoolIds ->
        schoolIds.isEmpty() ? List.of(NO_MATCHING_SCHOOL_ID) : schoolIds));
  }

  private Set<String> getCriterionValues(final List<SearchCriteria> criteriaList, final String key) {
    return criteriaList.stream()
      .filter(criteria -> StringUtils.equals(criteria.getKey(), key))
      .map(SearchCriteria::getValue)
      .filter(StringUtils::isNotBlank)
      .flatMap(value -> Arrays.stream(StringUtils.split(value, ",")))
      .map(StringUtils::trim)
      .filter(StringUtils::isNotBlank)
      .collect(Collectors.toCollection(LinkedHashSet::new));
  }

  private boolean containsIgnoreCase(final Set<String> values, final String candidate) {
    return values.stream().anyMatch(value -> StringUtils.equalsIgnoreCase(value, candidate));
  }

  private boolean isScopedCriterion(final SearchCriteria criteria) {
    return StringUtils.equals(criteria.getKey(), DISTRICT_ID) || StringUtils.equals(criteria.getKey(), SCHOOL_CATEGORY_CODE);
  }
}
