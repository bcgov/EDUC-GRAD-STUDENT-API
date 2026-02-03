package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OptionalProgramReport {
    // From GRADUATION_STUDENT_RECORD
    private String pen;
    private String studentStatus;
    private String legalFirstName;
    private String legalLastName;
    private String legalMiddleNames;
    private LocalDate dob;
    private String studentGrade;
    private String program;
    private LocalDate programCompletionDate;
    private String schoolOfRecord;
    private UUID schoolOfRecordId;
    private String schoolAtGraduation;
    private UUID schoolAtGraduationId;

    // From STUDENT_OPTIONAL_PROGRAM
    private UUID optionalProgramId;
    private LocalDate optionalProgramCompletionDate;
}
