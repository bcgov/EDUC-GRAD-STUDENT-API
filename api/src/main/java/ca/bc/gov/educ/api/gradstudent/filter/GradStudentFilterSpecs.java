package ca.bc.gov.educ.api.gradstudent.filter;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class GradStudentFilterSpecs extends BaseFilterSpecs<GraduationStudentRecordEntity> {

  public GradStudentFilterSpecs(FilterSpecifications<GraduationStudentRecordEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<GraduationStudentRecordEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<GraduationStudentRecordEntity, Integer> integerFilterSpecifications, FilterSpecifications<GraduationStudentRecordEntity, String> stringFilterSpecifications, FilterSpecifications<GraduationStudentRecordEntity, Long> longFilterSpecifications, FilterSpecifications<GraduationStudentRecordEntity, UUID> uuidFilterSpecifications, FilterSpecifications<GraduationStudentRecordEntity, Boolean> booleanFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, converters);
  }
}
