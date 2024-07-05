package ca.bc.gov.educ.api.gradstudent.util;

import ca.bc.gov.educ.api.gradstudent.exception.EntityNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

@Service
@Scope(proxyMode = ScopedProxyMode.DEFAULT)
public class GradValidation {

	private static final ThreadLocal<List<String>> warningList = ThreadLocal.<List<String>>withInitial(LinkedList::new);
	private static final ThreadLocal<List<String>> errorList = ThreadLocal.<List<String>>withInitial(LinkedList::new);

	@Autowired
	ca.bc.gov.educ.api.gradstudent.util.MessageHelper messagesHelper;
	
	public void addWarning(String warningMessage) {
		warningList.get().add(warningMessage);
	}

	public void addWarning(String formattedWarningMessage, Object... args) {
		warningList.get().add(String.format(formattedWarningMessage, args));
	}

	public void addError(String errorMessage) {
		errorList.get().add(errorMessage);
	}

	public void addError(String formattedErrorMessage, Object... args) {
		errorList.get().add(String.format(formattedErrorMessage, args));
	}

	public void addErrorAndStop(String errorMessage) {
		errorList.get().add(errorMessage);
		throw new ca.bc.gov.educ.api.gradstudent.util.GradBusinessRuleException(String.join(",\n", errorList.get()));

	}

	public void addErrorAndStop(String formattedErrorMessage, Object... args) {
		errorList.get().add(String.format(formattedErrorMessage, args));
		throw new ca.bc.gov.educ.api.gradstudent.util.GradBusinessRuleException(String.join(",\n", errorList.get()));

	}

	public void addNotFoundErrorAndStop(String errorMessage) {
		errorList.get().add(errorMessage);
		throw new EntityNotFoundException(String.join(",\n", errorList.get()));
	}
	
	public List<String> getWarnings() {
		return warningList.get();
	}
	
	public List<String> getErrors() {
		return errorList.get();
	}
	
    public void ifErrors(Consumer<List<String>> action) {
        if (!errorList.get().isEmpty()) {
            action.accept(errorList.get());
        }
    }

    public void ifWarnings(Consumer<List<String>> action) {
        if (!warningList.get().isEmpty()) {
            action.accept(warningList.get());
        }
    }
    
    public boolean requiredField(Object requiredValue, String fieldName) {
    	if (requiredValue == null) {
    		addError(messagesHelper.missingValue(fieldName));
    		return false;
    	}
    	if (requiredValue instanceof String && StringUtils.isBlank((String)requiredValue)) {
			addError(messagesHelper.missingValue(fieldName));
			return false;
    	}
		if (requiredValue instanceof List && ((List<?>) requiredValue).isEmpty()) {
			addError(messagesHelper.missingValue(fieldName));
			return false;
		}
    	return true;
    }
    
    public void stopOnErrors() {
    	if (hasErrors()) {
    		throw new ca.bc.gov.educ.api.gradstudent.util.GradBusinessRuleException(String.join(",\n", errorList.get()));
    	}
    }

	public void stopOnNotFoundErrors() {
		if (hasErrors()) {
			throw new EntityNotFoundException(String.join(",\n", errorList.get()));
		}
	}

	public boolean hasErrors() {
    	return !errorList.get().isEmpty();
    }
    
    public boolean hasWarnings() {
    	return !warningList.get().isEmpty();
    }
    
    public void clear() {
    	errorList.get().clear();
    	warningList.get().clear();
		errorList.remove();
		warningList.remove();
    }
}