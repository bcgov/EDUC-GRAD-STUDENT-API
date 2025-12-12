package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Component
public class GraduationStudentPaginationRecord extends BaseModel {

    private UUID studentID;
    private String pen;
    private String legalFirstName;
    private String legalMiddleNames;
    private String legalLastName;
    private Date dob;
    private String genderCode;
    private String program;
    private String gpa;
    private String studentStatus;
    private String honoursStanding;
    private Date programCompletionDate;
    private String recalculateGradStatus;
    private String schoolOfRecord;
    private String studentGrade;
    private String studentGradData;
    private String schoolAtGraduation;
    private String recalculateProjectedGrad;
    private Long batchId;
    private String consumerEducReqtMet;
    private String studentProjectedGradData;
    private String studentCitizenshipCode;
    private Date adultStartDate;
    private UUID schoolOfRecordId;
    private UUID schoolAtGraduationId;

}
