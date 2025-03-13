package ca.bc.gov.educ.api.gradstudent.exception;

/**
 * The type Pen reg api runtime exception.
 */
public class GradStudentAPIRuntimeException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 5241655513745148898L;

  /**
   * Instantiates a new Pen reg api runtime exception.
   *
   * @param message the message
   */
  public GradStudentAPIRuntimeException(String message) {
		super(message);
	}

  public GradStudentAPIRuntimeException(Throwable exception) {
    super(exception);
  }

}
