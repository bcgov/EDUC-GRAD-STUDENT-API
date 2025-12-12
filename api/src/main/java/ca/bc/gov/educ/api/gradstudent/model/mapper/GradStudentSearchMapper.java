package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.GradStudentSearchData;
import ca.bc.gov.educ.api.gradstudent.model.entity.GradStudentSearchDataEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class})
public interface GradStudentSearchMapper {

    GradStudentSearchMapper mapper = Mappers.getMapper(GradStudentSearchMapper.class);

    GradStudentSearchData toStructure(GradStudentSearchDataEntity student);

    GradStudentSearchDataEntity toModel(GradStudentSearchData gradStudentSearchData);

}
