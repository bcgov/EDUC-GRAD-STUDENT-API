package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCourse;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;


@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class, BigIntegerMapper.class})
public interface StudentCourseMapper {

    StudentCourseMapper mapper = Mappers.getMapper(StudentCourseMapper.class);

    @Mapping(target = "finalPercent", source = "finalPercent")
    @Mapping(target = "finalLetterGrade", source = "finalLetterGrade")
    StudentCourse toStructure(StudentCourseEntity entity);

    @Mapping(target = "finalPercent", source = "finalPercent")
    @Mapping(target = "finalLetterGrade", source = "finalLetterGrade")
    StudentCourseEntity toEntity(StudentCourse studentCourse);
}
