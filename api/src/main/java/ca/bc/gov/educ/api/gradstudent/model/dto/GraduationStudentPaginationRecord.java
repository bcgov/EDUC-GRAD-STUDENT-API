package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GraduationStudentPaginationRecord extends BaseModel {

    private String pen;
    private UUID schoolOfRecordId;
    private String studentGrade;
    private String studentStatus;
    private UUID studentID;

}
