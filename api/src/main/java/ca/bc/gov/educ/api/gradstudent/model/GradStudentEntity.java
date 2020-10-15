package ca.bc.gov.educ.api.gradstudent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.util.Date;

/**
 * The type Pen demographics entity.
 */
@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Immutable
@Table(name = "STUDENT_MASTER")
public class GradStudentEntity {

    @Id
    @Column(name = "STUD_NO")
    private String pen;

    @Column(name = "STUD_SURNAME")
    private String studSurname;

    @Column(name = "STUD_GIVEN")
    private String studGiven;

    @Column(name = "STUD_MIDDLE")
    private String studMiddle;

    @Column(name = "STUD_BIRTH")
    private String studBirth;

    @Column(name = "STUD_SEX")
    private String studSex;

    @Column(name = "ADDRESS1")
    private String address1;

    @Column(name = "ADDRESS2")
    private String address2;

    @Column(name = "CITY")
    private String city;

    @Column(name = "POSTAL")
    private String postalCode;

    @Column(name = "PROV_CODE")
    private String provinceCode;

    //@Column(name = "PROV_NAME")
    //private String provinceName;

    @Column(name = "CNTRY_CODE")
    private String countryCode;

    //@Column(name = "CNTRY_NAME")
    //private String countryName;

    @Column(name = "STUD_GRADE")
    private String studentGrade;

    @Column(name = "STUD_LOCAL_ID")
    private String studentLocalId;

    @Column(name = "STUD_CITIZ")
    private String studentCitizenship;

    @Column(name = "MINCODE")
    private String mincode;

    @Column(name = "STUD_STATUS")
    private String studStatus;

    @Column(name = "ARCHIVE_FLAG")
    private String archiveFlag;

    @Column(name = "GRAD_REQT_YEAR")
    private int gradRequirementYear;

    @Column(name = "PRGM_CODE")
    private String programCode;

    @Column(name = "PRGM_CODE2")
    private String programCode2;

    @Column(name = "PRGM_CODE3")
    private String programCode3;

    @Column(name = "PRGM_CODE4")
    private String programCode4;

    @Column(name = "PRGM_CODE5")
    private String programCode5;

    //@Column(name = "PRGM_NAME")
    //private String programName;

}
