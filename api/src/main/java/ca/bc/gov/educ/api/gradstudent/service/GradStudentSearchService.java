package ca.bc.gov.educ.api.gradstudent.service;

import ca.bc.gov.educ.api.gradstudent.exception.GradStudentAPIRuntimeException;
import ca.bc.gov.educ.api.gradstudent.filter.GradStudentSearchFilterSpecs;
import ca.bc.gov.educ.api.gradstudent.model.dto.Search;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStudentSearchDataEntity;
import ca.bc.gov.educ.api.gradstudent.repository.GradStudentSearchRepository;
import ca.bc.gov.educ.api.gradstudent.util.RequestUtil;
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

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class GradStudentSearchService extends PaginationService {

    @Getter
    private final GradStudentSearchFilterSpecs gradStudentSearchFilterSpecs;

    private final GradStudentSearchRepository gradStudentSearchRepository;

    private final Executor paginatedQueryExecutor = new EnhancedQueueExecutor.Builder()
            .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("async-pagination-query-executor-%d").build())
            .setCorePoolSize(2).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();

    @Transactional(propagation = Propagation.SUPPORTS)
    public CompletableFuture<Page<GradStudentSearchDataEntity>> findAll(Specification<GradStudentSearchDataEntity> studentSpecs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
        return CompletableFuture.supplyAsync(() -> {
            Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
            try {
                log.trace("Running paginated query: {}", studentSpecs);
                var results = this.gradStudentSearchRepository.findAll(studentSpecs, paging);
                log.trace("Paginated query returned with results: {}", results);
                return results;
            } catch (final Throwable ex) {
                log.error("Failure querying for paginated students: {}", ex.getMessage());
                throw new CompletionException(ex);
            }
        }, paginatedQueryExecutor);
    }

    public Specification<GradStudentSearchDataEntity> setSpecificationAndSortCriteria(String sortCriteriaJson, String searchCriteriaListJson, ObjectMapper objectMapper, List<Sort.Order> sorts) {
        Specification<GradStudentSearchDataEntity> studentSpecs = null;
        try {
            RequestUtil.getSortCriteria(sortCriteriaJson, objectMapper, sorts);
            if(StringUtils.isNotBlank(searchCriteriaListJson)) {
                List<Search> searches = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {});
                int i =  0;
                for (Search search : searches) {
                    studentSpecs = getSpecifications(studentSpecs, i, search, gradStudentSearchFilterSpecs);
                    i++;
                }
            }
        } catch (JsonProcessingException ex) {
            throw new GradStudentAPIRuntimeException(ex.getMessage());
        }
        return studentSpecs;
    }
}
