package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.StudentGradeCode;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentGradeCodeEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StudentGradeCodeTransformer {

  @Autowired
  ModelMapper modelMapper;

  public StudentGradeCode transformToDTO (StudentGradeCodeEntity entity) {
    return modelMapper.map(entity, StudentGradeCode.class);
  }

  public StudentGradeCodeEntity transformToEntity(StudentGradeCode studentGradeCode) {
    return modelMapper.map(studentGradeCode, StudentGradeCodeEntity.class);
  }
}
