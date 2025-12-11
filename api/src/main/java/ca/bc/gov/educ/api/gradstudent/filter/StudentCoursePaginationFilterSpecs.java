package ca.bc.gov.educ.api.gradstudent.filter;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCoursePaginationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class StudentCoursePaginationFilterSpecs extends BaseFilterSpecs<StudentCoursePaginationEntity> {

  public StudentCoursePaginationFilterSpecs(FilterSpecifications<StudentCoursePaginationEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<StudentCoursePaginationEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<StudentCoursePaginationEntity, Integer> integerFilterSpecifications, FilterSpecifications<StudentCoursePaginationEntity, String> stringFilterSpecifications, FilterSpecifications<StudentCoursePaginationEntity, Long> longFilterSpecifications, FilterSpecifications<StudentCoursePaginationEntity, UUID> uuidFilterSpecifications, FilterSpecifications<StudentCoursePaginationEntity, Boolean> booleanFilterSpecifications, FilterSpecifications<StudentCoursePaginationEntity, Date> utilDateFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, utilDateFilterSpecifications, converters);
  }
}
