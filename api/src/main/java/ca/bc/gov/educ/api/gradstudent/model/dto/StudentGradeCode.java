package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
@SuppressWarnings("squid:S1700")
public class StudentGradeCode extends BaseModel {
  private String studentGradeCode;
  private String label;
  private int displayOrder;
  private String description;
  private LocalDateTime effectiveDate;
  private LocalDateTime expiryDate;
  private String expected;
}
