package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.exception.GradStudentAPIRuntimeException;
import ca.bc.gov.educ.api.gradstudent.filter.BaseFilterSpecs;
import ca.bc.gov.educ.api.gradstudent.filter.GradStudentPaginationFilterSpecs;
import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradStudentPaginationRepository;
import ca.bc.gov.educ.api.gradstudent.util.RequestUtil;
import ca.bc.gov.educ.api.gradstudent.util.TransformUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.InvalidParameterException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class GradStudentPaginationService {
  @Getter
  private final GradStudentPaginationFilterSpecs gradStudentPaginationFilterSpecs;

  private final GradStudentPaginationRepository gradStudentPaginationRepository;

  private final Executor paginatedQueryExecutor = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("async-pagination-query-executor-%d").build())
    .setCorePoolSize(2).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();

  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<GraduationStudentRecordPaginationEntity>> findAll(Specification<GraduationStudentRecordPaginationEntity> studentSpecs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
    log.trace("In find all query: {}", studentSpecs);
    return CompletableFuture.supplyAsync(() -> {
      Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
      try {
        log.trace("Running paginated query: {}", studentSpecs);
        var results = this.gradStudentPaginationRepository.findAll(studentSpecs, paging);
        log.trace("Paginated query returned with results: {}", results);
        return results;
      } catch (final Throwable ex) {
        log.error("Failure querying for paginated SDC school students: {}", ex.getMessage());
        throw new CompletionException(ex);
      }
    }, paginatedQueryExecutor);

  }

  public Specification<GraduationStudentRecordPaginationEntity> setSpecificationAndSortCriteria(String sortCriteriaJson, String searchCriteriaListJson, ObjectMapper objectMapper, List<Sort.Order> sorts) {
    Specification<GraduationStudentRecordPaginationEntity> schoolSpecs = null;
    try {
      RequestUtil.getSortCriteria(sortCriteriaJson, objectMapper, sorts);
      if (StringUtils.isNotBlank(searchCriteriaListJson)) {
        List<Search> searches = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {
        });
        int i = 0;
        for (var search : searches) {
          schoolSpecs = getSpecifications(schoolSpecs, i, search, gradStudentPaginationFilterSpecs);
          i++;
        }
      }
    } catch (JsonProcessingException e) {
      throw new GradStudentAPIRuntimeException(e.getMessage());
    }
    return schoolSpecs;
  }

  public <T> Specification<T> getSpecifications(Specification<T> specs, int i, Search search, BaseFilterSpecs<T> filterSpecs) {
    if (i == 0) {
      specs = getEntitySpecification(search.getSearchCriteriaList(), filterSpecs);
    } else {
      if (search.getCondition() == Condition.AND) {
        specs = specs.and(getEntitySpecification(search.getSearchCriteriaList(), filterSpecs));
      } else {
        specs = specs.or(getEntitySpecification(search.getSearchCriteriaList(), filterSpecs));
      }
    }
    return specs;
  }

  private <T> Specification<T> getEntitySpecification(List<SearchCriteria> criteriaList, BaseFilterSpecs<T> filterSpecs) {
    Specification<T> specs = null;
    if (!criteriaList.isEmpty()) {
      int i = 0;
      for (SearchCriteria criteria : criteriaList) {
        if (criteria.getKey() != null && criteria.getOperation() != null && criteria.getValueType() != null) {
          var criteriaValue = criteria.getValue();
          if(StringUtils.isNotBlank(criteria.getValue()) && TransformUtil.isUppercaseField(GraduationStudentRecordPaginationEntity.class, criteria.getKey())) {
            criteriaValue = criteriaValue.toUpperCase();
          }
          Specification<T> typeSpecification = getTypeSpecification(criteria.getKey(), criteria.getOperation(), criteriaValue, criteria.getValueType(), filterSpecs);
          specs = getSpecificationPerGroup(specs, i, criteria, typeSpecification);
          i++;
        } else {
          throw new InvalidParameterException("Search Criteria can not contain null values for key, value and operation type");
        }
      }
    }
    return specs;
  }

  private <T> Specification<T> getSpecificationPerGroup(Specification<T> entitySpecification, int i, SearchCriteria criteria, Specification<T> typeSpecification) {
    if (i == 0) {
      entitySpecification = Specification.where(typeSpecification);
    } else {
      if (criteria.getCondition() == Condition.AND) {
        entitySpecification = entitySpecification.and(typeSpecification);
      } else {
        entitySpecification = entitySpecification.or(typeSpecification);
      }
    }
    return entitySpecification;
  }

  private <T> Specification<T> getTypeSpecification(String key, FilterOperation filterOperation, String value, ValueType valueType, BaseFilterSpecs<T> filterSpecs) {
    Specification<T> entitySpecification = null;
    switch (valueType) {
      case STRING ->
              entitySpecification = filterSpecs.getStringTypeSpecification(key, value, filterOperation);
      case DATE_TIME ->
              entitySpecification = filterSpecs.getDateTimeTypeSpecification(key, value, filterOperation);
      case LONG ->
              entitySpecification = filterSpecs.getLongTypeSpecification(key, value, filterOperation);
      case INTEGER ->
              entitySpecification = filterSpecs.getIntegerTypeSpecification(key, value, filterOperation);
      case DATE ->
              entitySpecification = filterSpecs.getDateTypeSpecification(key, value, filterOperation);
      case UUID ->
              entitySpecification = filterSpecs.getUUIDTypeSpecification(key, value, filterOperation);
      case BOOLEAN ->
              entitySpecification = filterSpecs.getBooleanTypeSpecification(key, value, filterOperation);
    }
    return entitySpecification;
  }
}
