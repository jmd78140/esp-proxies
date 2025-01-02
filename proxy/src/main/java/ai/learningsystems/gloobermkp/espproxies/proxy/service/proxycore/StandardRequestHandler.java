package ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore;


import static ai.learningsystems.gloobermkp.apigateway.domains.share.GatewayHeaderCustomFields.SERVICEREF_NOT_SET_IN_HEADER;
import static ai.learningsystems.gloobermkp.apigateway.domains.share.GatewayHeaderCustomFields.X_GMKP_XSP_SERVICE_REF;
import static ai.learningsystems.gloobermkp.espproxies.services.share.ProxyHeaderCustomFields.X_GMKP_XSP_TECHNICALMETRICS;
import static ai.learningsystems.gloobermkp.espproxies.services.share.ProxyHeaderCustomFields.X_GMKP_XSP_USAGEMETRICS;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.client.WebClient;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandler;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IUsageMetricsSupplier;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.technical.ResponseTime;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.technical.TechnicalMetrics;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.UsageMetrics;
import ai.learningsystems.gloobermkp.external.commons.domains.web.HttpRequestBuilder;
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

    
    final WebClient                client;
    private final IServiceHandler  serviceHandler;
    private final TechnicalMetrics technicalMetrics;


    public StandardRequestHandler(final WebClient client, final IServiceHandler serviceHandler) {

        this.client           = client;
        this.serviceHandler   = serviceHandler;
        this.technicalMetrics = new TechnicalMetrics();
    }


    /**
     * Handles the request by applying service-specific customizations and executing
     * it through the configured WebClient, with support for circuit breaking and
     * rate limiting.
     *
     * @param retargetedRequest the {@link ServerHttpRequest} containing the request
     *                          details.
     * @param requestBody       the body of the HTTP request.
     * @param circuitBreaker    the {@link CircuitBreaker} applied to the request
     *                          execution.
     * @param rateLimiter       the {@link RateLimiter} applied to the request
     *                          execution.
     * @return a {@link Flux} representing the response to the request.
     */
    @SuppressWarnings("unchecked")
    public Flux<?> handle(final ServerHttpRequest retargetedRequest, final String requestBody,
            final CircuitBreaker circuitBreaker, final RateLimiter rateLimiter)
    {

        return serviceHandler.handleRequest(retargetedRequest, requestBody)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter)).map(response -> {
                    log.debug("Received response: {}", ((ResponseEntity<String>) response).getBody());
                    return buildResponseWithMetrics(retargetedRequest, requestBody, (ResponseEntity<String>) response,
                            serviceHandler);
                }).onErrorResume(throwable -> handleFallback(throwable).flux());
    }


    /**
     * Executes an HTTP request using the provided retargeted request and returns
     * the response as a {@link Mono} containing a {@link ResponseEntity} with a
     * String body.
     *
     * @param retargetedRequest the {@link ServerHttpRequest} containing the request
     *                          details.
     * @param requestBody       the body of the HTTP request.
     * @return a {@link Mono} containing the response entity with enriched headers
     *         and body.
     */
    public Mono<ResponseEntity<String>> executeRequest(final ServerHttpRequest retargetedRequest,
            final String requestBody)
    {
        
        String serviceRef = SERVICEREF_NOT_SET_IN_HEADER;
        final var serviceRefHeaderValues = retargetedRequest.getHeaders().get(X_GMKP_XSP_SERVICE_REF);
        if( null != serviceRefHeaderValues && ! serviceRefHeaderValues.isEmpty() )
            serviceRef = serviceRefHeaderValues.get(0);
        final String servicePath = retargetedRequest.getPath() //
                .pathWithinApplication() //
                .value();
        
        final HttpRequestBuilder requestBuilder   = new HttpRequestBuilder(client, retargetedRequest, requestBody);
        final long               requestStartTime = System.currentTimeMillis();

        final String staticServiceRef = serviceRef;
        return requestBuilder.build() //
                .retrieve() //
                .toEntity(String.class) //
                .flatMap(responseEntity -> {
           
                    final long requestEndTime = System.currentTimeMillis();
                    technicalMetrics.addMetricToComponent(staticServiceRef, servicePath, new ResponseTime(requestStartTime, requestEndTime));
        
                    String jsonTechnicalMetrics = technicalMetrics.toJson();
                    log.debug("Adding technical metrics to response: {}", jsonTechnicalMetrics);
        
                    HttpHeaders modifiedHeaders = new HttpHeaders();
                    modifiedHeaders.addAll(responseEntity.getHeaders());
                    modifiedHeaders.add(X_GMKP_XSP_TECHNICALMETRICS, jsonTechnicalMetrics);
        
                    return Mono.just(ResponseEntity.status(responseEntity.getStatusCode()) //
                                .headers(modifiedHeaders)
                                .body(responseEntity.getBody())
                            );
                    
                }) //
                .doOnError(error -> log.error("Error occurred while retrieving the response", error));
    }


    /**
     * Adds custom usage metrics to the HTTP response headers.
     *
     * @param request     the {@link ServerHttpRequest} containing the request
     *                    details.
     * @param requestBody the body of the HTTP request.
     * @param response    the {@link ResponseEntity} containing the response
     *                    details.
     * @param handler     the {@link IUsageMetricsSupplier} used to compute usage
     *                    metrics.
     * @return a {@link ResponseEntity} with updated headers containing usage
     *         metrics.
     */
    private ResponseEntity<String> buildResponseWithMetrics(final ServerHttpRequest request, final String requestBody,
            final ResponseEntity<String> response, IUsageMetricsSupplier handler)
    {

        UsageMetrics usageMetrics     = handler.getMetrics(request, requestBody, response);
        String       jsonUsageMetrics = usageMetrics.toJson();

        HttpHeaders modifiedHeaders = new HttpHeaders();
        modifiedHeaders.addAll(response.getHeaders());
        modifiedHeaders.add(X_GMKP_XSP_USAGEMETRICS, jsonUsageMetrics);

        return ResponseEntity.status(response.getStatusCode()).headers(modifiedHeaders).body(response.getBody());
    }


    /**
     * Handles errors by returning a fallback response with a 503 status code.
     *
     * @param throwable the error that occurred during the request handling.
     * @return a {@link Mono} containing the fallback response entity.
     */
    private Mono<ResponseEntity<String>> handleFallback(final Throwable throwable)
    {

        log.error("Error during request execution: {}", throwable.getMessage(), throwable);
        return Mono.just(ResponseEntity.status(503).body("Service temporarily unavailable: " + throwable.getMessage()));
    }

}
