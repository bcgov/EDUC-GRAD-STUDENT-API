package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseReport {
    // From GRADUATION_STUDENT_RECORD
    private String pen;
    private String studentStatus;
    private String legalLastName;
    private LocalDate dob;
    private String studentGrade;
    private String program;
    private LocalDate programCompletionDate;
    private String schoolOfRecord;
    private UUID schoolOfRecordId;
    private String schoolAtGraduation;
    private UUID schoolAtGraduationId;

    // From STUDENT_COURSE
    private BigInteger courseID;
    private String courseSession;
    private Double interimPercent;
    private String interimLetterGrade;
    private Double finalPercent;
    private String finalLetterGrade;
    private Integer credits;
    private String equivOrChallenge;
    private String fineArtsAppliedSkillsCode;
    private UUID studentExamId;
}
