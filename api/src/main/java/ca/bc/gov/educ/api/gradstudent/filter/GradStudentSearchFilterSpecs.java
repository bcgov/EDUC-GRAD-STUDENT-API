package ca.bc.gov.educ.api.gradstudent.filter;

import ca.bc.gov.educ.api.gradstudent.model.entity.GradStudentSearchDataEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class GradStudentSearchFilterSpecs extends BaseFilterSpecs<GradStudentSearchDataEntity> {

  public GradStudentSearchFilterSpecs(FilterSpecifications<GradStudentSearchDataEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<GradStudentSearchDataEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<GradStudentSearchDataEntity, Integer> integerFilterSpecifications, FilterSpecifications<GradStudentSearchDataEntity, String> stringFilterSpecifications, FilterSpecifications<GradStudentSearchDataEntity, Long> longFilterSpecifications, FilterSpecifications<GradStudentSearchDataEntity, UUID> uuidFilterSpecifications, FilterSpecifications<GradStudentSearchDataEntity, Boolean> booleanFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, converters);
  }

}
