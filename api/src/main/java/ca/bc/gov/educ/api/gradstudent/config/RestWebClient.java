package ca.bc.gov.educ.api.gradstudent.config;

import ca.bc.gov.educ.api.gradstudent.util.EducGradStudentApiConstants;
import ca.bc.gov.educ.api.gradstudent.util.LogHelper;
import ca.bc.gov.educ.api.gradstudent.util.ThreadLocalStateUtil;
import io.netty.handler.logging.LogLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

@Configuration
@Profile("!test")
public class RestWebClient {

    @Autowired
    EducGradStudentApiConstants constants;

    private final HttpClient httpClient;

    public RestWebClient() {
        this.httpClient = HttpClient.create(ConnectionProvider.create("student-api")).compress(true)
                .resolver(spec -> spec.queryTimeout(Duration.ofMillis(200)).trace("DNS", LogLevel.TRACE));
        this.httpClient.warmup().block();
    }

    @Bean
    public WebClient webClient() {
        DefaultUriBuilderFactory defaultUriBuilderFactory = new DefaultUriBuilderFactory();
        defaultUriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);
        return WebClient.builder().uriBuilderFactory(defaultUriBuilderFactory).exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(40 * 1024 * 1024)) // 40 MB
                      .build())
                .filter(setRequestHeaders())
                .filter(this.log())
                .build();
    }

    private ExchangeFilterFunction log() {
        return (clientRequest, next) -> next
            .exchange(clientRequest)
            .doOnNext((clientResponse -> LogHelper.logClientHttpReqResponseDetails(
                    clientRequest.method(),
                    clientRequest.url().toString(),
                    //GRAD2-1929 Refactoring/Linting replaced rawStatusCode() with statusCode as it was deprecated
                    // clientResponse.rawStatusCode(),
                    clientResponse.statusCode().value(),
                    clientRequest.headers().get(EducGradStudentApiConstants.CORRELATION_ID),
                    clientRequest.headers().get(EducGradStudentApiConstants.REQUEST_SOURCE),
                    constants.isSplunkLogHelperEnabled())
            ));
    }

    private ExchangeFilterFunction setRequestHeaders() {
        return (clientRequest, next) -> {
            ClientRequest modifiedRequest = ClientRequest.from(clientRequest)
                    .header(EducGradStudentApiConstants.CORRELATION_ID, ThreadLocalStateUtil.getCorrelationID())
                    .header(EducGradStudentApiConstants.USER_NAME, ThreadLocalStateUtil.getCurrentUser())
                    .header(EducGradStudentApiConstants.REQUEST_SOURCE, EducGradStudentApiConstants.API_NAME)
                    .build();
            return next.exchange(modifiedRequest);
        };
    }
}
