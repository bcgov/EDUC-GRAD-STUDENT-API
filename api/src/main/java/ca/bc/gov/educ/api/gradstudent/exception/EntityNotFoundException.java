package ca.bc.gov.educ.api.gradstudent.exception;

import ca.bc.gov.educ.api.gradstudent.util.GradBusinessRuleException;

public class EntityNotFoundException extends GradBusinessRuleException {


    public EntityNotFoundException() {
        super();
    }

    public EntityNotFoundException(String message) {
        super(message);
    }

}
