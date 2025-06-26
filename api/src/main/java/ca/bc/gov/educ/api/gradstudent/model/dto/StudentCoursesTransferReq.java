package ca.bc.gov.educ.api.gradstudent.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCoursesTransferReq {
  @NotNull
  private UUID sourceStudentId;

  @NotNull
  private UUID targetStudentId;

  @NotEmpty
  private List<UUID> studentCourseIdsToMove;
}
