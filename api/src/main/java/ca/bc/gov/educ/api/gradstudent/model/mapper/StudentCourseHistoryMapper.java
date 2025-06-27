package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourseHistory;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class})
public interface StudentCourseHistoryMapper {

    StudentCourseHistoryMapper mapper = Mappers.getMapper(StudentCourseHistoryMapper.class);

    @Mapping(target = "finalPercent", source = "completedCoursePercentage")
    @Mapping(target = "finalLetterGrade", source = "completedCourseLetterGrade")
    @Mapping(target = "id", source = "historyId")
    StudentCourseHistory toStructure(StudentCourseHistoryEntity entity);

}
