package ca.bc.gov.educ.api.gradstudent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;

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

    /*@Column(name = "ARCHIVE_FLAG")
    private String archiveFlag;*/

    @Column(name = "STUD_SURNAME")
    private String studSurname;

    @Column(name = "STUD_GIVEN")
    private String studGiven;

    @Column(name = "STUD_MIDDLE")
    private String studMiddle;

    @Column(name = "ADDRESS1")
    private String address1;

    @Column(name = "ADDRESS2")
    private String address2;

    @Column(name = "CITY")
    private String city;

    @Column(name = "PROV_CODE")
    private String provinceCode;

    @Column(name = "CNTRY_CODE")
    private String countryCode;

    @Column(name = "POSTAL")
    private String postalCode;

    @Column(name = "STUD_BIRTH")
    private String studBirth;

    @Column(name = "STUD_SEX")
    private String studSex;

    @Column(name = "MINCODE")
    private String mincode;

/*    @Column(name = "STUD_CITIZ")
    private String studentCitizenship;

    @Column(name = "STUD_GRADE")
    private String studentGrade;

    @Column(name = "STUD_LOCAL_ID")
    private String studentLocalId;

    @Column(name = "STUD_TRUE_NO")
    private String studTrueNo;

    @Column(name = "STUD_SIN")
    private String studSin;

    @Column(name = "PRGM_CODE")
    private String programCode;

    @Column(name = "PRGM_CODE2")
    private String programCode2;

    @Column(name = "PRGM_CODE3")
    private String programCode3;

    @Column(name = "STUD_PSI_PERMIT")
    private String studPsiPermit;

    @Column(name = "STUD_RSRCH_PERMIT")
    private String studResearchPermit;

    @Column(name = "STUD_STATUS")
    private String studStatus;

    @Column(name = "STUD_CONSED_FLAG")
    private String studConsedFlag;

    @Column(name = "YR_ENTER_11")
    private String yrEnter11;

    @Column(name = "GRAD_DATE")
    private String gradDate;

    @Column(name = "DOGWOOD_FLAG")
    private String dogwoodFlag;

    @Column(name = "HONOUR_FLAG")
    private String honourFlag;

    @Column(name = "MINCODE_GRAD")
    private String mincode_grad;

    @Column(name = "FRENCH_DOGWOOD")
    private String frenchDogwood;

    @Column(name = "PRGM_CODE4")
    private String programCode4;

    @Column(name = "PRGM_CODE5")
    private String programCode5;

    @Column(name = "SCC_DATE")
    private String sccDate;

    @Column(name = "GRAD_REQT_YEAR")
    private String gradRequirementYear;

    @Column(name = "SLP_DATE")
    private String slpDate;

    @Column(name = "MERGED_FROM_PEN")
    private String mergedFromPen;

    @Column(name = "GRAD_REQT_YEAR_AT_GRAD")
    private String gradReqtYearAtGrad;

    @Column(name = "STUD_GRADE_AT_GRAD")
    private String studGradeAtGrad;

    @Column(name = "XCRIPT_ACTV_DATE")
    private String xcriptActvDate;

    @Column(name = "ALLOWED_ADULT")
    private String allowedAdult;

    @Column(name = "SSA_NOMINATION_DATE")
    private String ssaNominationDate;

    @Column(name = "ADJ_TEST_YEAR")
    private String adjTestYear;

    @Column(name = "GRADUATED_ADULT")
    private String graduatedAdult;

    @Column(name = "SUPPLIER_NO")
    private String supplierNo;

    @Column(name = "SITE_NO")
    private String siteNo;

    @Column(name = "EMAIL_ADDRESS")
    private String emailAddress;

    @Column(name = "ENGLISH_CERT")
    private String englishCert;

    @Column(name = "FRENCH_CERT")
    private String frenchCert;

    @Column(name = "ENGLISH_CERT_DATE")
    private String englishCertDate;

    @Column(name = "FRENCH_CERT_DATE")
    private String frenchCertDate;*/

}
