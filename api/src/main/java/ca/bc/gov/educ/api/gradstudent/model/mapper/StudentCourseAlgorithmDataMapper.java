package ca.bc.gov.educ.api.gradstudent.model.mapper;

import ca.bc.gov.educ.api.gradstudent.model.dto.external.algorithm.v1.StudentCourseAlgorithmData;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCourseEntity;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, StringMapper.class, BigIntegerMapper.class})
@DecoratedWith(StudentCourseAlgorithmDataMapperDecorator.class)
public interface StudentCourseAlgorithmDataMapper {
  StudentCourseAlgorithmDataMapper mapper = Mappers.getMapper(StudentCourseAlgorithmDataMapper.class);

  @Mapping(target = "courseCode", source = "courseID")
  @Mapping(target = "relatedCourse", source = "relatedCourseId")
  @Mapping(target = "provExamCourse", expression = "java(entity.getCourseExam() != null ? \"Y\" : \"N\")")
  @Mapping(target = "schoolPercent", source = "entity.courseExam.schoolPercentage")
  @Mapping(target = "bestSchoolPercent", source = "entity.courseExam.bestSchoolPercentage")
  @Mapping(target = "bestExamPercent", source = "entity.courseExam.bestExamPercentage")
  @Mapping(target = "specialCase", source = "entity.courseExam.specialCase")
  @Mapping(target = "examPercent", source = "entity.courseExam.examPercentage")
  @Mapping(target = "toWriteFlag", source = "entity.courseExam.toWriteFlag")
  @Mapping(target = "sessionDate", ignore = true)
  @Mapping(target = "completedCoursePercentage", source = "finalPercent")
  @Mapping(target = "completedCourseLetterGrade", source = "finalLetterGrade")
  StudentCourseAlgorithmData toStructure(StudentCourseEntity entity);
}
