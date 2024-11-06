package ca.bc.gov.educ.api.gradstudent.model.dc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationEvent extends Event {
  private String sagaStatus;
  private String sagaName;
}
