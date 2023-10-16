package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.util.UUID;

@Data
@Entity
@Table(name = "STUDENT_NONGRAD_REASONS_VW")
public class StudentNonGradReasonEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "STUDENT_GUID", nullable = false)
    private UUID graduationStudentRecordId;

    @Column(name = "STUDENT_PEN")
    private String pen;

    @Column(name = "TRANSCRIPTRULE1")
    private String transcriptRule1;

    @Column(name = "DESCRIPTION1")
    private String description1;

    @Column(name = "GRADRULE1")
    private String gradRule1;

    @Column(name = "PROJECTED1")
    private String projected1;

    @Column(name = "TRANSCRIPTRULE2")
    private String transcriptRule2;

    @Column(name = "DESCRIPTION2")
    private String description2;

    @Column(name = "GRADRULE2")
    private String gradRule2;

    @Column(name = "PROJECTED2")
    private String projected2;

    @Column(name = "TRANSCRIPTRULE3")
    private String transcriptRule3;

    @Column(name = "DESCRIPTION3")
    private String description3;

    @Column(name = "GRADRULE3")
    private String gradRule3;

    @Column(name = "PROJECTED3")
    private String projected3;

    @Column(name = "TRANSCRIPTRULE4")
    private String transcriptRule4;

    @Column(name = "DESCRIPTION4")
    private String description4;

    @Column(name = "GRADRULE4")
    private String gradRule4;

    @Column(name = "PROJECTED4")
    private String projected4;

    @Column(name = "TRANSCRIPTRULE5")
    private String transcriptRule5;

    @Column(name = "DESCRIPTION5")
    private String description5;

    @Column(name = "GRADRULE5")
    private String gradRule5;

    @Column(name = "PROJECTED5")
    private String projected5;

    @Column(name = "TRANSCRIPTRULE6")
    private String transcriptRule6;

    @Column(name = "DESCRIPTION6")
    private String description6;

    @Column(name = "GRADRULE6")
    private String gradRule6;

    @Column(name = "PROJECTED6")
    private String projected6;

    @Column(name = "TRANSCRIPTRULE7")
    private String transcriptRule7;

    @Column(name = "DESCRIPTION7")
    private String description7;

    @Column(name = "GRADRULE7")
    private String gradRule7;

    @Column(name = "PROJECTED7")
    private String projected7;

    @Column(name = "TRANSCRIPTRULE8")
    private String transcriptRule8;

    @Column(name = "DESCRIPTION8")
    private String description8;

    @Column(name = "GRADRULE8")
    private String gradRule8;

    @Column(name = "PROJECTED8")
    private String projected8;

    @Column(name = "TRANSCRIPTRULE9")
    private String transcriptRule9;

    @Column(name = "DESCRIPTION9")
    private String description9;

    @Column(name = "GRADRULE9")
    private String gradRule9;

    @Column(name = "PROJECTED9")
    private String projected9;

    @Column(name = "TRANSCRIPTRULE10")
    private String transcriptRule10;

    @Column(name = "DESCRIPTION10")
    private String description10;

    @Column(name = "GRADRULE10")
    private String gradRule10;

    @Column(name = "PROJECTED10")
    private String projected10;

    @Column(name = "TRANSCRIPTRULE11")
    private String transcriptRule11;

    @Column(name = "DESCRIPTION11")
    private String description11;

    @Column(name = "GRADRULE11")
    private String gradRule11;

    @Column(name = "PROJECTED11")
    private String projected11;

    @Column(name = "TRANSCRIPTRULE12")
    private String transcriptRule12;

    @Column(name = "DESCRIPTION12")
    private String description12;

    @Column(name = "GRADRULE12")
    private String gradRule12;

    @Column(name = "PROJECTED12")
    private String projected12;

}
