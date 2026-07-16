package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class SnapshotResponse {
    private String pen;
    private String graduatedDate; // yyyyMM
    private BigDecimal gpa;
    private String honourFlag;
    private String schoolOfRecord;
    private String schoolOfRecordId;
    private String studentGrade;
}
