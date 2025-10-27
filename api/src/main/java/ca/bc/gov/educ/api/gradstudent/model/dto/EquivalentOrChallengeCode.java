package ca.bc.gov.educ.api.gradstudent.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.ReadOnlyProperty;

import java.math.BigInteger;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EquivalentOrChallengeCode extends BaseModel {

    @ReadOnlyProperty
    private String equivalentOrChallengeCode;

    @ReadOnlyProperty
    private String label;

    @ReadOnlyProperty
    private String description;

    @ReadOnlyProperty
    private BigInteger displayOrder;

    @ReadOnlyProperty
    private String effectiveDate;

    @ReadOnlyProperty
    private String expiryDate;

}
