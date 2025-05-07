package ca.bc.gov.educ.api.gradstudent.model.dto;

public interface GraduationCountProjection {
    String getSchoolOfRecordId();
    
    Long getCurrentGraduates();

    Long getCurrentNonGraduates();
}