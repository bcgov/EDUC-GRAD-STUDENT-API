package ca.bc.gov.educ.api.gradstudent.repository;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Getter
@Setter
@Builder
public class GraduationStudentRecordSearchCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<UUID> schoolIds;
    private List<UUID> districtIds;
    private List<UUID> studentIds;
    private List<String> programs;

    LocalDate gradDateFrom;
    LocalDate gradDateTo;

    String activityCode;

}
