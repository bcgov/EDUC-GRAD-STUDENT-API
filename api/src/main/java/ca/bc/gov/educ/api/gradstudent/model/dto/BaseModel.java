package ca.bc.gov.educ.api.gradstudent.model.dto;

import ca.bc.gov.educ.api.gradstudent.util.GradLocalDateTimeDeserializer;
import ca.bc.gov.educ.api.gradstudent.util.GradLocalDateTimeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BaseModel {
	private String createUser;
	@JsonSerialize(using = GradLocalDateTimeSerializer.class)
	@JsonDeserialize(using = GradLocalDateTimeDeserializer.class)
	private LocalDateTime createDate;
	private String updateUser;
	@JsonSerialize(using = GradLocalDateTimeSerializer.class)
	@JsonDeserialize(using = GradLocalDateTimeDeserializer.class)
	private LocalDateTime updateDate;

	public LocalDateTime getUpdateDate() {
		return updateDate == null ? LocalDateTime.now() : updateDate;
	}

	public LocalDateTime getCreateDate() {
		return createDate == null ? LocalDateTime.now() : createDate;
	}
}
