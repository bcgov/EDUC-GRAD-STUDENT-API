package ca.bc.gov.educ.api.gradstudent.filter;

import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class StudentOptionalProgramPaginationFilterSpecs extends BaseFilterSpecs<StudentOptionalProgramPaginationEntity> {

  public StudentOptionalProgramPaginationFilterSpecs(FilterSpecifications<StudentOptionalProgramPaginationEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<StudentOptionalProgramPaginationEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<StudentOptionalProgramPaginationEntity, Integer> integerFilterSpecifications, FilterSpecifications<StudentOptionalProgramPaginationEntity, String> stringFilterSpecifications, FilterSpecifications<StudentOptionalProgramPaginationEntity, Long> longFilterSpecifications, FilterSpecifications<StudentOptionalProgramPaginationEntity, UUID> uuidFilterSpecifications, FilterSpecifications<StudentOptionalProgramPaginationEntity, Boolean> booleanFilterSpecifications, FilterSpecifications<StudentOptionalProgramPaginationEntity, Date> utilDateFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, utilDateFilterSpecifications, converters);
  }
}

