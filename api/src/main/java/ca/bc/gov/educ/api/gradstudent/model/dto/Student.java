package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import jakarta.validation.constraints.*;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Student {

    /**
     * The Student id.
     */
    String studentID;
    /**
     * The Pen.
     */
    @NotNull(message = "PEN Number can not be null.")
    String pen;
    /**
     * The Legal first name.
     */
    @Size(max = 25)
    String legalFirstName;
    /**
     * The Legal middle names.
     */
    @Size(max = 25)
    String legalMiddleNames;
    /**
     * The Legal last name.
     */
    @Size(max = 25)
    @NotNull(message = "Legal Last Name can not be null.")
    String legalLastName;
    /**
     * The Dob.
     */
    @NotNull(message = "Date of Birth can not be null.")
    String dob;
    /**
     * The Sex code.
     */
    @NotNull(message = "Sex Code can not be null.")
    String sexCode;
    /**
     * The Gender code.
     */
    String genderCode;
	/**
	 * The citizenship code.
	 */
	String citizenship;
    /**
     * The Usual first name.
     */
    @Size(max = 25)
    String usualFirstName;
    /**
     * The Usual middle names.
     */
    @Size(max = 25)
    String usualMiddleNames;
    /**
     * The Usual last name.
     */
    @Size(max = 25)
    String usualLastName;
    /**
     * The Email.
     */
    @Size(max = 80)
    @Email(message = "Email must be valid email address.")
    String email;
    /**
     * The Email verified.
     */
    @NotNull(message = "Email verified cannot be null.")
    @Size(max = 1)
    @Pattern(regexp = "[YN]")
    String emailVerified;
    /**
     * The Deceased date.
     */
    String deceasedDate;
    /**
     * The Postal code.
     */
    @Size(max = 6)
    String postalCode;
    /**
     * The Mincode.
     */
    @Size(max = 8)
    String mincode;
    /**
     * The Local id.
     */
    @Size(max = 12)
    String localID;
    /**
     * The Grade code.
     */
    @Size(max = 2)
    String gradeCode;
    /**
     * The Grade year.
     */
    @Size(max = 4)
    String gradeYear;
    /**
     * The Demog code.
     */
    @Size(max = 1)
    String demogCode;
    /**
     * The Status code.
     */
    @Size(max = 1)
    String statusCode;
    /**
     * The Memo.
     */
    @Size(max = 4000)
    String memo;
    /**
     * The True student id.
     */
    String trueStudentID;

    /**
     * The Create user.
     */
    @Size(max = 100)
    public String createUser;
    /**
     * The Update user.
     */
    @Size(max = 100)
    public String updateUser;
    /**
     * The Create date.
     */
    @Null(message = "createDate should be null.")
    public String createDate;
    /**
     * The Update date.
     */
    @Null(message = "updateDate should be null.")
    public String updateDate;

    String historyActivityCode;
    String documentTypeCode;
    String dateOfConfirmation;

}
