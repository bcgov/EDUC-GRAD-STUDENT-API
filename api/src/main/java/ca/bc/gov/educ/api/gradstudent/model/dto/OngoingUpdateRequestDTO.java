package ca.bc.gov.educ.api.gradstudent.model.dto;

import ca.bc.gov.educ.api.gradstudent.constant.TraxEventType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class OngoingUpdateRequestDTO {
    private String studentID;
    private String pen;
    private TraxEventType eventType;
    private List<OngoingUpdateFieldDTO> updateFields = new ArrayList<>();
}
