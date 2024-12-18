package ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore;


import java.net.URI;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
import ai.learningsystems.gloobermkp.external.commons.domains.web.RequestModifier;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



/**
 * Generic proxy service for redirecting requests to third-party services and
 * adding calculated usage metrics in the HTTP response header.
 */
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


    public boolean isStreamReplyRequested(final ServerHttpRequest request, final String requestBody)
    {


        final String                 serviceName           = extractServiceName(request);
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

        return serviceHandler.isStreamReplyRequested(request, requestBody);

    }


    /**
     * Proxies an incoming HTTP request to a target service endpoint based on the provided configuration.
     * <p>
     * This method handles the logic for routing requests to the appropriate service handler based on 
     * the service name extracted from the request. It supports both standard and streaming requests, 
     * applying provider-specific configurations, circuit breaker policies, and rate limiting as necessary.
     * The target service URI is dynamically resolved based on the service configuration.
     * </p>
     * 
     * @param request the {@link ServerHttpRequest} representing the original incoming HTTP request. 
     *                This contains the method, headers, query parameters, and other request details.
     * @param requestBody a {@link String} containing the body of the HTTP request. This is required for 
     *                    POST, PUT, or other methods where the body contains meaningful data. Can be {@code null} 
     *                    if the request does not have a body.
     * @return a {@link Flux} that represents the proxied response, which could be a standard response 
     *         or a stream of data in case of streaming requests. Each emitted element corresponds to 
     *         a part of the response.
     * @throws RuntimeException if no suitable service handler or service configuration is found for the requested service.
     *         This includes cases such as missing service properties, invalid configurations, or unregistered handlers.
     */
    public Flux<?> proxyRequest(final ServerHttpRequest request, final String requestBody)
    {

        String                      serviceName          = extractServiceName(request);
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

        boolean isStreamingRequested = serviceHandler.isStreamReplyRequested(request, requestBody);

        URI targetServicePath = serviceHandlerRegistry.getTargetServiceURI(serviceName);
        if (null == targetServicePath)
            return handleBadRequest("Service has no target service URL.");

        final ServerHttpRequest retargetedRequest =  RequestModifier.modifyUri(request, targetServicePath);

        client = configureWebClient(properties);
        try {

            final CircuitBreaker circuitBreaker = circuitBreakerManagerService.getCircuitBreaker(serviceName,
                    properties.getCircuitBreakerConfiguration());
            final RateLimiter    rateLimiter    = rateLimiterManagerService.getRateLimiter(serviceName,
                    properties.getRateLimiterConfiguration());

            return isStreamingRequested
                    ? handleStreamingRequest(serviceHandler, retargetedRequest, requestBody, circuitBreaker,
                            rateLimiter)
                    : handleStandardRequest(serviceHandler, retargetedRequest, requestBody, circuitBreaker,
                            rateLimiter);
        }
        catch (BeansException e) {
            return Mono.error(new RuntimeException("Handler not found for service: " + serviceName, e)).flux();
        }
    }


    /**
     * Executes an HTTP request to the specified endpoint with the provided request details.
     * <p>
     * This method delegates the execution to a {@link StandardRequestHandler}, which is responsible for 
     * handling the HTTP request using the configured WebClient and service handler. It supports dynamic 
     * configuration of headers, query parameters, and request body.
     * </p>
     * 
     * @param retargetedRequest the {@link ServerHttpRequest} representing the retargeted HTTP request. Contains headers, 
     *                query parameters, and method details.
     * @return a {@link Mono} containing the {@link ResponseEntity<String>} with the response from the target service.
     *         The response includes the status code, headers, and body returned by the target service.
     */
    @Override
    public Mono<ResponseEntity<String>> executeRequest(final ServerHttpRequest retargetedRequest, final String requestBody)
    {

        final StandardRequestHandler standardRequestHandler = new StandardRequestHandler(client, serviceHandler);
        return standardRequestHandler.executeRequest(retargetedRequest, requestBody);
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
    public Flux<ServerSentEvent<String>> executeStreamingRequest(final ServerHttpRequest retargetedRequest, final String requestBody)
    {

        final StreamingRequestHandler streamingRequestHandler = new StreamingRequestHandler(client, serviceHandler);
        return streamingRequestHandler.executeStreamingRequest(retargetedRequest, requestBody);
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


    private Flux<?> handleBadRequest(final String message)
    {

        return Mono.just(ResponseEntity.badRequest().body(message)).flux();
    }


    private Flux<?> handleStandardRequest(final IServiceHandler serviceHandler,
            final ServerHttpRequest retargetedRequest, final String requestBody, final CircuitBreaker circuitBreaker,
            final RateLimiter rateLimiter)
    {

        final StandardRequestHandler standardRequestHandler = new StandardRequestHandler(client, serviceHandler);
        return standardRequestHandler.handle(retargetedRequest, requestBody, circuitBreaker, rateLimiter);
    }


    private Flux<?> handleStreamingRequest(final IServiceHandler serviceHandler,
            final ServerHttpRequest retargetedRequest, final String requestBody, final CircuitBreaker circuitBreaker,
            final RateLimiter rateLimiter)
    {

        final StreamingRequestHandler streamingRequestHandler = new StreamingRequestHandler(client, serviceHandler);
        return streamingRequestHandler.handle(retargetedRequest, requestBody, circuitBreaker, rateLimiter);
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
    private String extractServiceName(final ServerHttpRequest request)
    {

        final String fullEndpoint = request.getURI().getRawPath();
        return fullEndpoint;
    }


}
