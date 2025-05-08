package ca.bc.gov.educ.api.gradstudent.model.dto;

import java.util.UUID;

public interface GraduationCountProjection {
    UUID getSchoolOfRecordId();
    
    Long getCurrentGraduates();

    Long getCurrentNonGraduates();
}