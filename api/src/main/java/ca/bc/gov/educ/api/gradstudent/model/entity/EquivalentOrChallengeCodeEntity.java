package ca.bc.gov.educ.api.gradstudent.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "EQUIVALENT_OR_CHALLENGE_CODE")
public class EquivalentOrChallengeCodeEntity extends BaseEntity {

    @Id
    @Column(name = "EQUIVALENT_OR_CHALLENGE_CODE", nullable = false)
    private String equivalentOrChallengeCode;

    @Column(name = "LABEL", nullable = false, length = 50)
    private String label;

    @Column(name = "DESCRIPTION", nullable = false, length = 355)
    private String description;

    @Column(name = "DISPLAY_ORDER", nullable = false, precision = 0)
    private BigInteger displayOrder;

    @Column(name = "EFFECTIVE_DATE", nullable = false)
    private LocalDate effectiveDate;

    @Column(name = "EXPIRY_DATE", nullable = true)
    private LocalDate expiryDate;

}

