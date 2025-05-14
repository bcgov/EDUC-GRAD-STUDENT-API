package ca.bc.gov.educ.api.gradstudent.model.transformer;

import ca.bc.gov.educ.api.gradstudent.model.dto.*;
import ca.bc.gov.educ.api.gradstudent.model.entity.GraduationStudentRecordPaginationEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.ReportGradStudentDataEntity;
import ca.bc.gov.educ.api.gradstudent.model.entity.StudentCoursePaginationEntity;
import ca.bc.gov.educ.api.gradstudent.util.JsonTransformer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Component
public class GradStudentPaginationTransformer {

    private static final Logger logger = LoggerFactory.getLogger(GradStudentPaginationTransformer.class);

    @Autowired
    ModelMapper modelMapper;

    public GraduationStudentPaginationRecord transformToDTO (GraduationStudentRecordPaginationEntity entity) {
        var student = modelMapper.map(entity, GraduationStudentPaginationRecord.class);
        student.setStudentCourses(new ArrayList<>());
        if(entity.getStudentCoursePaginationEntities() != null && !entity.getStudentCoursePaginationEntities().isEmpty()) {
            entity.getStudentCoursePaginationEntities().stream().forEach(studentCoursePaginationEntity -> {
                student.getStudentCourses().add(modelMapper.map(studentCoursePaginationEntity, StudentCoursePagination.class));
            });
        }
    	return student;
    }

}
