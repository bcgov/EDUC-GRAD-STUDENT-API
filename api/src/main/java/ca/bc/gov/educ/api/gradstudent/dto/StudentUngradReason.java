package ca.bc.gov.educ.api.gradstudent.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class StudentUngradReason extends BaseModel {

    private UUID studentUngradReasonID;
    private UUID graduationStudentRecordID;
    private String ungradReasonCode;
    private String ungradReasonDescription;
}
