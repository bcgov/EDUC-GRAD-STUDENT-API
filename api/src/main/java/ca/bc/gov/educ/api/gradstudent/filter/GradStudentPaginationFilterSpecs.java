package ca.bc.gov.educ.api.gradstudent.filter;

import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordPaginationEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class GradStudentPaginationFilterSpecs extends BaseFilterSpecs<GraduationStudentRecordPaginationEntity> {

  public GradStudentPaginationFilterSpecs(FilterSpecifications<GraduationStudentRecordPaginationEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<GraduationStudentRecordPaginationEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<GraduationStudentRecordPaginationEntity, Integer> integerFilterSpecifications, FilterSpecifications<GraduationStudentRecordPaginationEntity, String> stringFilterSpecifications, FilterSpecifications<GraduationStudentRecordPaginationEntity, Long> longFilterSpecifications, FilterSpecifications<GraduationStudentRecordPaginationEntity, UUID> uuidFilterSpecifications, FilterSpecifications<GraduationStudentRecordPaginationEntity, Boolean> booleanFilterSpecifications, FilterSpecifications<GraduationStudentRecordPaginationEntity, Date> utilDateFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, utilDateFilterSpecifications, converters);
  }
}
