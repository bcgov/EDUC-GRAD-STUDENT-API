package ca.bc.gov.educ.api.gradstudent.service;


import ca.bc.gov.educ.api.gradstudent.exception.ServiceException;
import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;


import java.io.IOException;
import java.time.Duration;

@Service
public class RESTService {

    private WebClient webClient;

    private static final String SERVER_ERROR = "5xx error.";
    private static final String SERVICE_FAILED_ERROR = "Service failed to process after max retries.";

    @Autowired
    public RESTService(@Qualifier("studentApiClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public <T> T get(String url, Class<T> clazz, WebClient webClient) {
        T obj;
        if (webClient == null)
            webClient = this.webClient;
        try {
            obj = webClient
                    .get()
                    .uri(url)
                    .headers(h -> h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID()))
                    .retrieve()
                    // if 5xx errors, throw Service error
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> Mono.error(new ServiceException(getErrorMessage(url, SERVER_ERROR), clientResponse.statusCode().value())))
                    .bodyToMono(clazz)
                    // only does retry if initial error was 5xx as service may be temporarily down
                    // 4xx errors will always happen if 404, 401, 403 etc, so does not retry
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(ex -> ex instanceof ServiceException || ex instanceof IOException || ex instanceof WebClientRequestException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                throw new ServiceException(getErrorMessage(url, SERVICE_FAILED_ERROR), HttpStatus.SERVICE_UNAVAILABLE.value());
                            }))
                    .block();
        } catch (Exception e) {
            // catches IOExceptions and the like
            throw new ServiceException(
                    getErrorMessage(url, e.getLocalizedMessage()),
                    (e instanceof WebClientResponseException webClientResponseException) ? webClientResponseException.getStatusCode().value()
                            : HttpStatus.SERVICE_UNAVAILABLE.value(), e);
        }
        return obj;
    }

    /**
     * Overloaded get method that allows for a ParameterizedTypeReference to be passed in.
     * You can call this method if you are expecting a complex type or a list of objects.
     * Example usage:
     * <pre>
     *     	List<Student> students = this.restService.get(
     * 			String.format(constants.getPenStudentApiByPenUrl(), pen),
     * 			new ParameterizedTypeReference<List<Student>>() {},
     * 			null
     * 		);
     * </pre>
     * @param url the url you are calling
     * @param typeReference the ParameterizedTypeReference for the expected return type
     * @param webClient the WebClient to use for the request, if null, will use the default webClient
     * @return the expected return type
     * @param <T> expected return type
     */
    public <T> T get(String url, ParameterizedTypeReference<T> typeReference, WebClient webClient) {
        T obj;
        if (webClient == null)
            webClient = this.webClient;
        try {
            obj = webClient
                    .get()
                    .uri(url)
                    .headers(h -> h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID()))
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> Mono.error(new ServiceException(getErrorMessage(url, SERVER_ERROR), clientResponse.statusCode().value())))
                    .bodyToMono(typeReference)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(ex -> ex instanceof ServiceException || ex instanceof IOException || ex instanceof WebClientRequestException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                throw new ServiceException(getErrorMessage(url, SERVICE_FAILED_ERROR), HttpStatus.SERVICE_UNAVAILABLE.value());
                            }))
                    .block();
        } catch (Exception e) {
            throw new ServiceException(
                    getErrorMessage(url, e.getLocalizedMessage()),
                    (e instanceof WebClientResponseException webClientResponseException) ? webClientResponseException.getStatusCode().value()
                            : HttpStatus.SERVICE_UNAVAILABLE.value(), e);
        }
        return obj;
    }

    public <T> T post(String url, Object body, Class<T> clazz, WebClient webClient) {
        T obj;
        if (webClient == null)
            webClient = this.webClient;
        try {
            obj = webClient.post()
                    .uri(url)
                    .headers(h -> h.set(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID()))
                    .body(BodyInserters.fromValue(body))
                    .retrieve()
                    .onStatus(HttpStatusCode::is5xxServerError,
                            clientResponse -> Mono.error(new ServiceException(getErrorMessage(url, SERVER_ERROR), clientResponse.statusCode().value())))
                    .bodyToMono(clazz)
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(2))
                            .filter(ex -> ex instanceof ServiceException || ex instanceof IOException || ex instanceof WebClientRequestException)
                            .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                                throw new ServiceException(getErrorMessage(url, SERVICE_FAILED_ERROR), HttpStatus.SERVICE_UNAVAILABLE.value());
                            }))
                    .block();
        } catch (Exception e) {
            throw new ServiceException(getErrorMessage(
                    url,
                    e.getLocalizedMessage()),
                    (e instanceof WebClientResponseException webClientResponseException) ? (webClientResponseException.getStatusCode().value())
                            : HttpStatus.SERVICE_UNAVAILABLE.value(), e);
        }
        return obj;
    }

    private String getErrorMessage(String url, String errorMessage) {
        return "Service failed to process at url: " + url + " due to: " + errorMessage;
    }
}

