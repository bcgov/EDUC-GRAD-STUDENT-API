package ca.bc.gov.educ.api.gradstudent.exception;

import ca.bc.gov.educ.api.gradstudent.exception.errors.ApiError;
import lombok.Getter;

/**
 * The type Invalid payload exception.
 */
@SuppressWarnings("squid:S1948")
public class InvalidPayloadException extends RuntimeException {

  /**
   * The Error.
   */
  @Getter
  private final ApiError error;

  /**
   * Instantiates a new Invalid payload exception.
   *
   * @param error the error
   */
  public InvalidPayloadException(final ApiError error) {
    super(error.getMessage());
    this.error = error;
  }
}
