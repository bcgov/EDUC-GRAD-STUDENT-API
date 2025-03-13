package ca.bc.gov.educ.api.gradstudent.filter;

import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class GradStudentFilterSpecs extends BaseFilterSpecs<ReportGradStudentDataEntity> {

  public GradStudentFilterSpecs(FilterSpecifications<ReportGradStudentDataEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<ReportGradStudentDataEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<ReportGradStudentDataEntity, Integer> integerFilterSpecifications, FilterSpecifications<ReportGradStudentDataEntity, String> stringFilterSpecifications, FilterSpecifications<ReportGradStudentDataEntity, Long> longFilterSpecifications, FilterSpecifications<ReportGradStudentDataEntity, UUID> uuidFilterSpecifications, FilterSpecifications<ReportGradStudentDataEntity, Boolean> booleanFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, converters);
  }
}
