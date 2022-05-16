package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class StudentUndoCompletionReason extends BaseModel {

    private UUID studentUndoCompletionReasonID;
    private UUID graduationStudentRecordID;
    private String undoCompletionReasonCode;
    private String undoCompletionReasonDescription;
}
