package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.EquivalentOrChallengeCode;
import ca.bc.gov.educ.api.gradstudent.model.entity.EquivalentOrChallengeCodeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class})
public interface EquivalentOrChallengeCodeMapper {

    EquivalentOrChallengeCodeMapper mapper = Mappers.getMapper(EquivalentOrChallengeCodeMapper.class);

    EquivalentOrChallengeCode toStructure(EquivalentOrChallengeCodeEntity entity);

    EquivalentOrChallengeCodeEntity toEntity(EquivalentOrChallengeCode session);
}
