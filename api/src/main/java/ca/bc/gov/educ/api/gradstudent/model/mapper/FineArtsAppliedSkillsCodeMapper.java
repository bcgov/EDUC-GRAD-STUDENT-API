package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.FineArtsAppliedSkillsCode;
import ca.bc.gov.educ.api.gradstudent.model.entity.FineArtsAppliedSkillsCodeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class})
public interface FineArtsAppliedSkillsCodeMapper {

    FineArtsAppliedSkillsCodeMapper mapper = Mappers.getMapper(FineArtsAppliedSkillsCodeMapper.class);

    FineArtsAppliedSkillsCode toStructure(FineArtsAppliedSkillsCodeEntity entity);

    FineArtsAppliedSkillsCodeEntity toEntity(FineArtsAppliedSkillsCode session);
}

