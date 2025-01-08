package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Table(name = "REPORT_GRAD_SCHOOL_YE_VW")
public class ReportGradSchoolYearEndEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @jakarta.persistence.Id
    @Column(name = "SCHOOL_ID")
    private UUID schoolId;

}
