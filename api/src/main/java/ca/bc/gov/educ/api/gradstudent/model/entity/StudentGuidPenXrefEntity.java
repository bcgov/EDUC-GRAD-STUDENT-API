package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "STUDENT_GUID_PEN_XREF")
public class StudentGuidPenXrefEntity extends BaseEntity {

    @Id
    @Column(name = "STUDENT_GUID", nullable = false)
    private UUID studentID;
    
    @Column(name = "STUDENT_PEN", nullable = false)
    private String pen;
}