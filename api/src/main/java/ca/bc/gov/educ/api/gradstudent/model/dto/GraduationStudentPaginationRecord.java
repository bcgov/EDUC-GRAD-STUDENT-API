package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants.DEFAULT_DATE_FORMAT;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GraduationStudentPaginationRecord extends BaseModel {

    private String pen;
    private UUID schoolOfRecordId;
    private String studentGrade;
    private String studentStatus;
    private UUID studentID;

    private List<StudentCoursePagination> studentCourses;
}
