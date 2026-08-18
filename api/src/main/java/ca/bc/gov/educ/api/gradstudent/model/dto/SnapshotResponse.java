package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class SnapshotResponse {
    private UUID studentID;
    private String pen;
    private String graduatedDate; // yyyyMM
    private BigDecimal gpa;
    private String honourFlag;
    private String schoolOfRecord;
    private String schoolOfRecordId;
    private String studentGrade;
}
