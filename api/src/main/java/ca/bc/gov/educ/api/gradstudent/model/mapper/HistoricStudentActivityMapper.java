package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.HistoricStudentActivity;
import ca.bc.gov.educ.api.gradstudent.model.entity.HistoricStudentActivityEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class})
public interface HistoricStudentActivityMapper {

    HistoricStudentActivityMapper mapper = Mappers.getMapper(HistoricStudentActivityMapper.class);

    HistoricStudentActivity toStructure(HistoricStudentActivityEntity entity);
}
