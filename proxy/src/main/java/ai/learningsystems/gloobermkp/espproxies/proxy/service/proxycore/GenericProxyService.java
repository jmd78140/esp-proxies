package ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore;


import java.net.URI;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.proxy.IProxyService;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandler;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandlerFactory;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.IServiceHandlerRegistry;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration.ServiceProperties;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * Generic proxy service for redirecting requests to third-party services and
 * adding calculated usage metrics in the HTTP response header.
 */
@Slf4j
@Service
public class GenericProxyService implements IProxyService
{


    private final WebClient.Builder            webClientBuilder;
    private final IServiceHandlerRegistry      serviceHandlerRegistry;
    private final CircuitBreakerManagerService circuitBreakerManagerService;
    private final RateLimiterManagerService    rateLimiterManagerService;
    @SuppressWarnings("unused")
    private final ApplicationContext           applicationContext;

    private WebClient       client;
    private IServiceHandler serviceHandler;


    /**
     * Constructs the GenericProxyService with the necessary dependencies.
     *
     * @param webClientBuilder       the WebClient builder for creating HTTP
     *                               clients.
     * @param serviceConfig          the service configuration containing ESP
     *                               properties.
     * @param circuitBreakerRegistry the registry for managing circuit breakers.
     * @param rateLimiterRegistry    the registry for managing rate limiters.
     * @param applicationContext     the application context for retrieving beans.
     */
    @Autowired
    public GenericProxyService(final WebClient.Builder webClientBuilder,
            final IServiceHandlerRegistry serviceHandlerRegistry,
            final CircuitBreakerManagerService circuitBreakerManagerService,
            final RateLimiterManagerService rateLimiterManagerService, final ApplicationContext applicationContext) {

        this.webClientBuilder             = webClientBuilder;
        this.serviceHandlerRegistry       = serviceHandlerRegistry;
        this.circuitBreakerManagerService = circuitBreakerManagerService;
        this.rateLimiterManagerService    = rateLimiterManagerService;
        this.applicationContext           = applicationContext;
    }


    public boolean isStreamReplyRequested(final String fullEndpoint, final HttpHeaders headers,
            final String requestBody, final Map<String, String> queryParams)
    {

        final String                 serviceName           = extractServiceName(fullEndpoint);
        final IServiceHandlerFactory serviceHandlerFactory = serviceHandlerRegistry
                .getServiceHandlerFactory(serviceName);
        serviceHandler = serviceHandlerFactory.createServiceHandler((IProxyService) this);
        if (serviceHandler == null)
            throw new RuntimeException();

        final ServiceHandlerConfiguration serviceConfig = serviceHandlerRegistry
                .getServiceHandlerConfiguration(serviceName);
        if (serviceConfig == null)
            throw new RuntimeException();

        final ServiceProperties properties = serviceConfig.getServiceProperties();
        if (properties == null)
            throw new RuntimeException();

        return serviceHandler.isStreamReplyRequested(headers, requestBody, queryParams);

    }


    /**
     * Main method for handling the proxy request. This method performs the entire
     * proxy flow including resolving the handler, applying resilience mechanisms,
     * and returning the response with custom metrics.
     *
     * @param fullEndpoint the full endpoint path (e.g.,
     *                     "/openai/chat/completions").
     * @param method       the HTTP method (GET, POST, etc.).
     * @param headers      the HTTP headers of the incoming request.
     * @param requestBody  the body of the incoming request.
     * @param queryParams  the query parameters of the request.
     * @return a Flux wrapping the HTTP response with added usage metrics.
     */
    public Flux<?> proxyRequest(final String fullEndpoint, final HttpMethod method, final HttpHeaders headers,
            final String requestBody, final Map<String, String> queryParams)
    {

        log.info(
                "proxyRequest() received request - fullEndpoint: {}, method: {}, headers: {}, requestBody: {}, queryParams: {}",
                fullEndpoint, method, headers, requestBody, queryParams);

        String                      serviceName          = extractServiceName(fullEndpoint);
        ServiceHandlerConfiguration serviceHandlerConfig = serviceHandlerRegistry
                .getServiceHandlerConfiguration(serviceName);
        if (null == serviceHandlerConfig)
            return handleBadRequest("Unknown service: " + serviceName);

        ServiceProperties properties = serviceHandlerConfig.getServiceProperties();
        if (null == properties)
            return handleBadRequest("Service has no associated properties.");

        final IServiceHandlerFactory serviceHandlerFactory = serviceHandlerRegistry
                .getServiceHandlerFactory(serviceName);
        serviceHandler = serviceHandlerFactory.createServiceHandler((IProxyService) this);
        if (serviceHandler == null)
            return handleBadRequest("Service handler not found.");

        boolean isStreamingRequested = serviceHandler.isStreamReplyRequested(headers, requestBody, queryParams);
        log.debug("Streaming requested: {}", isStreamingRequested);

        URI targetServicePath = serviceHandlerRegistry.getTargetServiceURI(serviceName);
        if (null == targetServicePath)
            return handleBadRequest("Service has no target service URL.");

        client = configureWebClient(properties);
        try {

            final CircuitBreaker circuitBreaker = circuitBreakerManagerService.getCircuitBreaker(serviceName,
                    properties.getCircuitBreakerConfiguration());
            final RateLimiter    rateLimiter    = rateLimiterManagerService.getRateLimiter(serviceName,
                    properties.getRateLimiterConfiguration());
          
            return isStreamingRequested
                    ? handleStreamingRequest(serviceHandler, targetServicePath, method, headers, requestBody,
                            queryParams, circuitBreaker, rateLimiter)
                    : handleStandardRequest(serviceHandler, targetServicePath, method, headers, requestBody,
                            queryParams, circuitBreaker, rateLimiter);
        }
        catch (BeansException e) {
            return Mono.error(new RuntimeException("Handler not found for service: " + serviceName, e)).flux();
        }
    }


    private Flux<?> handleBadRequest(String message)
    {

        return Mono.just(ResponseEntity.badRequest().body(message)).flux();
    }


    private Flux<?> handleStreamingRequest(IServiceHandler serviceHandler, URI targetServicePath, HttpMethod method,
            HttpHeaders headers, String requestBody, Map<String, String> queryParams, CircuitBreaker circuitBreaker,
            RateLimiter rateLimiter)
    {

        final StreamingRequestHandler streamingRequestHandler = new StreamingRequestHandler(client, serviceHandler);
        return streamingRequestHandler.handle(targetServicePath, method, headers, requestBody, queryParams,
                circuitBreaker, rateLimiter);
    }


    /**
     * Executes a streaming HTTP request and returns the response as a Flux of
     * String.
     * 
     * @param endpoint    the target endpoint for the request.
     * @param method      the HTTP method (GET, POST, etc.).
     * @param headers     the HTTP headers for the request.
     * @param requestBody the body of the request.
     * @param queryParams query parameters to be included in the request.
     * @return a Flux wrapping the HTTP streaming response body.
     */
    @Override
    public Flux<ServerSentEvent<String>> executeStreamingRequest(final URI endpoint, final HttpMethod method,
            final HttpHeaders headers, final String requestBody, final Map<String, String> queryParams)
    {

        final StreamingRequestHandler streamingRequestHandler = new StreamingRequestHandler(client, serviceHandler);
        return streamingRequestHandler.executeStreamingRequest(endpoint, method, headers, requestBody, queryParams);
    }


    private Flux<?> handleStandardRequest(IServiceHandler serviceHandler, URI targetServicePath, HttpMethod method,
            HttpHeaders headers, String requestBody, Map<String, String> queryParams, CircuitBreaker circuitBreaker,
            RateLimiter rateLimiter)
    {
        
        final StandardRequestHandler standardRequestHandler = new StandardRequestHandler(client, serviceHandler);
        return standardRequestHandler.handle(targetServicePath, method, headers, requestBody, queryParams,
                circuitBreaker, rateLimiter);
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
    @Override
    public Mono<ResponseEntity<String>> executeRequest(final URI endpoint, final HttpMethod method,
            final HttpHeaders headers, final String requestBody, final Map<String, String> queryParams)
    {

        final StandardRequestHandler standardRequestHandler = new StandardRequestHandler(client, serviceHandler);
        return standardRequestHandler.executeRequest(endpoint, method, headers, requestBody, queryParams);
    }


    /**
     * This suppress
     * 
     * @param serviceName
     */
    @Override
    public void cleanCacheForService(final String serviceName)
    {

        circuitBreakerManagerService.removeCircuitBreaker(serviceName);
        rateLimiterManagerService.removeRateLimiter(serviceName);
    }


    /**
     * Configures a WebClient instance using the provided service properties.
     * 
     * @param properties the service properties containing the configuration for the
     *                   WebClient.
     * @return a configured WebClient instance.
     */
    private WebClient configureWebClient(final ServiceProperties properties)
    {

        final MultiValueMap<String, String> customHeaders = new LinkedMultiValueMap<>();
        properties.getCustomHeaders().forEach(customHeaders::add);

        return webClientBuilder.baseUrl(properties.getTargetServiceBaseUrl())
                .defaultHeaders(h -> h.addAll(customHeaders)).build();
    }


    /**
     * Extracts the service name from the full endpoint path.
     * <p>
     * Currently, this method simply returns the full endpoint as the service name.
     * For example, if the full endpoint is {@code /openai/v1/chat/completions}, the
     * extracted service name will also be {@code /openai/v1/chat/completions}.
     * <p>
     * This approach ensures relative uniqueness for identifying services based on
     * their endpoint paths.
     * 
     * @param fullEndpoint the full endpoint path, typically including version and
     *                     resource information.
     * @return the extracted service name, which is the same as the provided full
     *         endpoint.
     */
    private String extractServiceName(final String fullEndpoint)
    {

        return fullEndpoint;
    }

}
