package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.ExamSpecialCaseCode;
import ca.bc.gov.educ.api.gradstudent.model.entity.ExamSpecialCaseCodeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class})
public interface ExamSpecialCaseCodeMapper {

    ExamSpecialCaseCodeMapper mapper = Mappers.getMapper(ExamSpecialCaseCodeMapper.class);

    ExamSpecialCaseCode toStructure(ExamSpecialCaseCodeEntity entity);

    ExamSpecialCaseCodeEntity toEntity(ExamSpecialCaseCode session);
}
