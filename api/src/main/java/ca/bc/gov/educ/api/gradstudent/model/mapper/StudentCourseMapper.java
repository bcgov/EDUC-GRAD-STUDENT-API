package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class})
public interface StudentCourseMapper {

    StudentCourseMapper mapper = Mappers.getMapper(StudentCourseMapper.class);

    @Mapping(target = "finalPercent", source = "completedCoursePercentage")
    @Mapping(target = "finalLetterGrade", source = "completedCourseLetterGrade")
    StudentCourse toStructure(StudentCourseEntity entity);

    @Mapping(target = "completedCoursePercentage", source = "finalPercent")
    @Mapping(target = "completedCourseLetterGrade", source = "finalLetterGrade")
    StudentCourseEntity toEntity(StudentCourse session);
}
