package ca.bc.gov.educ.api.gradstudent.repository;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Data
@Getter
@Setter
public class GraduationStudentRecordSearchCriteria implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<String> schoolOfRecords;
    private List<String> districts;
    private List<String> pens;
    private List<String> programs;
}
