package ca.bc.gov.educ.api.gradstudent.model.entity;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public interface GraduationStudentRecordView {

    public String getProgram();
    public Date getProgramCompletionDate();
    public String getGpa();
    public String getHonoursStanding();
    public String getRecalculateGradStatus();
    public String getSchoolOfRecord();
    public String getStudentGrade();
    public String getStudentStatus();
    public UUID getStudentID();
    public String getSchoolAtGrad();
    public String getRecalculateProjectedGrad();
    public Long getBatchId();
    public String getConsumerEducationRequirementMet();
    public String getStudentCitizenship();
    public Date getAdultStartDate();
    public String getStudentProjectedGradData() ;
    public UUID getSchoolOfRecordId();
    public UUID getSchoolAtGraduationId();
    public LocalDateTime getCreateDate();
    public LocalDateTime getUpdateDate();
}