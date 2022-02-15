package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.*;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@ToString(callSuper = true)
public class StudentCreate extends Student implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The History activity code.
	 */
	@NotNull(message = "historyActivityCode can not be null.")
	String historyActivityCode;

}
