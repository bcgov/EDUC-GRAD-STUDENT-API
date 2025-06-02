package ca.bc.gov.educ.api.gradstudent.exception;

public class EntityAlreadyExistsException extends RuntimeException {


    public EntityAlreadyExistsException() {
        super();
    }

    public EntityAlreadyExistsException(String message) {
        super(message);
    }

}
