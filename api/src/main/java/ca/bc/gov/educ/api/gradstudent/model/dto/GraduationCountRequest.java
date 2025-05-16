package ca.bc.gov.educ.api.gradstudent.model.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data 
public class GraduationCountRequest {
    private List<UUID> schoolID; 
}