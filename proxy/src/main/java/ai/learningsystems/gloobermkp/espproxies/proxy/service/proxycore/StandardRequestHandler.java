package ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore;

import static ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore.GenericProxyConstants.X_TECHNICALMETRICS;
import static ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore.GenericProxyConstants.X_USAGEMETRICS;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.technical.ESPServiceResponseTime;
import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.technical.TechnicalMetrics;
import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage.UsageMetrics;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandler;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IUsageMetricsSupplier;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
public class StandardRequestHandler
{

    final WebClient client;
    private final IServiceHandler serviceHandler;
    private final TechnicalMetrics technicalMetrics; 
    
    public StandardRequestHandler(final WebClient client, final IServiceHandler serviceHandler) {
        
        this.client = client;
        this.serviceHandler = serviceHandler;
        this.technicalMetrics = new TechnicalMetrics();
    }
    
    
    @SuppressWarnings("unchecked")
    public Flux<?> handle(final URI targetServicePath, final HttpMethod method, final HttpHeaders headers, 
            final String requestBody, final Map<String, String> queryParams, 
            final CircuitBreaker circuitBreaker, final RateLimiter rateLimiter)
    {
        // This indirection through ServiceHandler allow request customization on providers constraints
        return serviceHandler.handleRequest(targetServicePath, method, headers, requestBody, queryParams)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter))
                .map(response -> {
                    log.debug("Received response : " +((ResponseEntity<String>) response).getBody());
                    return buildResponseWithMetrics(requestBody, (ResponseEntity<String>) response, serviceHandler); 
                 })
                .onErrorResume(this::handleFallback);
    }
    
    
    /**
     * Executes an HTTP request and returns the response as a Mono containing a
     * String body.
     * 
     * @param endpoint    the target endpoint for the request.
     * @param method      the HTTP method (GET, POST, etc.).
     * @param headers     the HTTP headers for the request.
     * @param requestBody the body of the request.
     * @param queryParams query parameters to be included in the request.
     * @return a Mono wrapping the HTTP response entity with a String body.
     */
    public Mono<ResponseEntity<String>> executeRequest(final URI endpoint, final HttpMethod method,
            final HttpHeaders headers, final String requestBody, final Map<String, String> queryParams)
    {

        log.debug(
                "executeRequest() received request: endpoint = {}, method = {}, headers = {}, requestBody = {}, queryParams = {}",
                endpoint, method, headers, requestBody, queryParams);

        if (endpoint == null || method == null) {
            return Mono.error(new IllegalArgumentException("Endpoint and method must not be null"));
        }

        final TargetServiceRequestBuilder requestBuilder = new TargetServiceRequestBuilder(client, endpoint, method,
                headers, requestBody, queryParams);

        final long requestStartTime = System.currentTimeMillis();

        return requestBuilder.buildRequest()
                .retrieve()
                .toEntity(String.class)
                .flatMap(responseEntity -> {
                    final long requestEndTime = System.currentTimeMillis();
                    technicalMetrics.addMetric("ESP_RT", new ESPServiceResponseTime(requestStartTime, requestEndTime));
                    final String jsonTechnicalMetrics = technicalMetrics.toJson();
                    log.debug("Adding technical metrics to response: {}", jsonTechnicalMetrics);

                    HttpHeaders modifiedHeaders = new HttpHeaders();
                    modifiedHeaders.addAll(responseEntity.getHeaders());
                    modifiedHeaders.add(X_TECHNICALMETRICS, jsonTechnicalMetrics);

                    return Mono.just(ResponseEntity
                            .status(responseEntity.getStatusCode())
                            .headers(modifiedHeaders)
                            .body(responseEntity.getBody()));
                })
                .doOnError(error -> {
                    log.error("Error occurred while retrieving the response", error);
                });
    }
    
    /**
     * Builds the response entity by adding custom metrics from the handler to the
     * HTTP headers.
     * 
     * @param response the original response entity.
     * @param handler  the handler responsible for generating metrics.
     * @return the response entity with added usage metrics.
     */
    private ResponseEntity<String> buildResponseWithMetrics(final String requestBody,
            final ResponseEntity<String> response, final IUsageMetricsSupplier handler)
    {
        final UsageMetrics usageMetrics     = handler.getMetrics(requestBody, response);
        final String       jsonUsageMetrics = usageMetrics.toJson();
        return ResponseEntity.status(response.getStatusCode())
                .headers(headers -> headers.add(X_USAGEMETRICS, jsonUsageMetrics))
                .headers(headers -> headers.addAll(response.getHeaders()))
                .body(response.getBody());
    }

    /**
     * Handles fallback in case of an error, returning a 503 response.
     * 
     * @param throwable the error that occurred during the proxy request.
     * @return a Mono wrapping the fallback response entity.
     */
    private Mono<ResponseEntity<String>> handleFallback(final Throwable throwable)
    {

        return Mono.just(ResponseEntity.status(503).body("Service temporarily unavailable: " + throwable.getMessage()));
    }
}
