package ca.bc.gov.educ.api.gradstudent.exception;

import ca.bc.gov.educ.api.gradstudent.exception.errors.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * The type Rest exception handler.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    /**
     * The constant log.
     */
    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    /**
     * Handle http message not readable response entity.
     *
     * @param ex      the ex
     * @param headers the headers
     * @param status  the status
     * @param request the request
     * @return the response entity
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String error = "Malformed JSON request";
        log.error("{} ", error, ex);
        return buildResponseEntity(new ApiError(BAD_REQUEST, error, ex));
    }

    /**
     * Build response entity response entity.
     *
     * @param apiError the api error
     * @return the response entity
     */
    private ResponseEntity<Object> buildResponseEntity(ApiError apiError) {
        return new ResponseEntity<>(apiError, apiError.getStatus());
    }

    /**
     * Handles EntityNotFoundException. Created to encapsulate errors with more detail than jakarta.persistence.EntityNotFoundException.
     *
     * @param ex the EntityNotFoundException
     * @return the ApiError object
     */
    @ExceptionHandler(EntityNotFoundException.class)
    protected ResponseEntity<Object> handleEntityNotFound(
            EntityNotFoundException ex) {
        ApiError apiError = new ApiError(NOT_FOUND);
        apiError.setMessage(ex.getMessage());
        log.info("{} ", apiError.getMessage(), ex);
        return buildResponseEntity(apiError);
    }

    /**
     * Handles IllegalArgumentException
     *
     * @param ex the InvalidParameterException
     * @return the ApiError object
     */
    @ExceptionHandler(IllegalArgumentException.class)
    protected ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        log.error("{} ", apiError.getMessage(), ex);
        return buildResponseEntity(apiError);
    }

    /**
     * Handles InvalidParameterException
     *
     * @param ex the InvalidParameterException
     * @return the ApiError object
     */
    @ExceptionHandler(InvalidParameterException.class)
    protected ResponseEntity<Object> handleInvalidParameter(InvalidParameterException ex) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage(ex.getMessage());
        apiError.setTimestamp(LocalDateTime.now());
        log.error("{} ", apiError.getMessage(), ex);
        return buildResponseEntity(apiError);
    }

    /**
     * Handles MethodArgumentNotValidException. Triggered when an object fails @Valid validation.
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid validation fails
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        ApiError apiError = new ApiError(BAD_REQUEST);
        apiError.setMessage("Validation error");
        apiError.addValidationErrors(ex.getBindingResult().getFieldErrors());
        apiError.addValidationError(ex.getBindingResult().getGlobalErrors());
        log.error("{} ", apiError.getMessage(), ex);
        return buildResponseEntity(apiError);
    }

    /**
     * Handles EntityNotFoundException. Created to encapsulate errors with more detail than jakarta.persistence.EntityNotFoundException.
     *
     * @param ex the EntityNotFoundException
     * @return the ApiError object
     */
    @ExceptionHandler(InvalidPayloadException.class)
    protected ResponseEntity<Object> handleInvalidPayload(
            InvalidPayloadException ex) {
        log.error("", ex);
        return buildResponseEntity(ex.getError());
    }

}

