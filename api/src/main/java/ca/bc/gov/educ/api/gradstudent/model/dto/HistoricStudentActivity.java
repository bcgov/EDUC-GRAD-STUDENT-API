package ca.bc.gov.educ.api.gradstudent.model.dto;

import ca.bc.gov.educ.api.gradstudent.util.GradLocalDateTimeDeserializer;
import ca.bc.gov.educ.api.gradstudent.util.GradLocalDateTimeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class HistoricStudentActivity extends BaseModel {

    private UUID historicStudentActivityID;
    private UUID graduationStudentRecordID;

    @JsonSerialize(using = GradLocalDateTimeSerializer.class)
    @JsonDeserialize(using = GradLocalDateTimeDeserializer.class)
    private LocalDateTime date;
    
    private String type;
    private String program;
    private String userID;
    private String batch;
    private String seqNo;
}

