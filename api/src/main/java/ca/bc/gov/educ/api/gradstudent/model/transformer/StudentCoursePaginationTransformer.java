package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentPaginationRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentCoursePagination;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCoursePaginationEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class StudentCoursePaginationTransformer {

    @Autowired
    ModelMapper modelMapper;

    public StudentCoursePagination transformToDTO (StudentCoursePaginationEntity entity) {
        var studentCourse = modelMapper.map(entity, StudentCoursePagination.class);

        var student = modelMapper.map(entity.getGraduationStudentRecordEntity(), GraduationStudentPaginationRecord.class);
        studentCourse.setGradStudent(student);

    	return studentCourse;
    }

}
