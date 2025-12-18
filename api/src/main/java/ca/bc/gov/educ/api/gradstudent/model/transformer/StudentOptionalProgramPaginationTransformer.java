package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.GraduationStudentPaginationRecord;
import ca.bc.gov.educ.api.gradstudent.model.dto.StudentOptionalProgramPagination;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentOptionalProgramPaginationEntity;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class StudentOptionalProgramPaginationTransformer {

    @Autowired
    ModelMapper modelMapper;

    public StudentOptionalProgramPagination transformToDTO (StudentOptionalProgramPaginationEntity entity) {
        var studentOptionalProgram = modelMapper.map(entity, StudentOptionalProgramPagination.class);

        var student = modelMapper.map(entity.getGraduationStudentRecordEntity(), GraduationStudentPaginationRecord.class);
        studentOptionalProgram.setGradStudent(student);

    	return studentOptionalProgram;
    }

}

