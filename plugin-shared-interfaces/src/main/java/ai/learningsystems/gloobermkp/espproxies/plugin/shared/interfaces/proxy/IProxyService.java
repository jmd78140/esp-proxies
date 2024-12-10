package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.proxy;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Interface provided by the proxy to plugins for executing HTTP requests and managing service-related operations.
 * <p>
 * This interface defines methods for executing both standard and streaming HTTP requests, as well as for managing 
 * cache invalidation for specific services.
 */
public interface IProxyService {

    /**
     * Executes a standard HTTP request to the specified endpoint and returns the response.
     * 
     * @param endpoint the {@link URI} of the target endpoint.
     * @param method the {@link HttpMethod} representing the HTTP method to use (e.g., GET, POST).
     * @param headers a {@link HttpHeaders} object containing the HTTP headers for the request.
     * @param requestBody a {@link String} representing the body of the HTTP request, or {@code null} if no body is required.
     * @param queryParams a {@link Map} containing query parameters as key-value pairs to be appended to the URL.
     * @return a {@link Mono} of {@link ResponseEntity} containing the HTTP response, encapsulating the status code and body.
     */
    Mono<ResponseEntity<String>> executeRequest(final URI endpoint, 
                                                final HttpMethod method,
                                                final HttpHeaders headers,
                                                final String requestBody,
                                                final Map<String, String> queryParams);

    /**
     * Executes a streaming HTTP request (e.g., Server-Sent Events) to the specified endpoint and returns the stream of events.
     * 
     * @param endpoint the {@link URI} of the target endpoint.
     * @param method the {@link HttpMethod} representing the HTTP method to use (e.g., GET, POST).
     * @param headers a {@link HttpHeaders} object containing the HTTP headers for the request.
     * @param requestBody a {@link String} representing the body of the HTTP request, or {@code null} if no body is required.
     * @param queryParams a {@link Map} containing query parameters as key-value pairs to be appended to the URL.
     * @return a {@link Flux} of {@link ServerSentEvent} containing the streamed response, where each event represents a chunk of data.
     */
    Flux<ServerSentEvent<String>> executeStreamingRequest(final URI endpoint, 
                                                          final HttpMethod method,
                                                          final HttpHeaders headers, 
                                                          final String requestBody, 
                                                          final Map<String, String> queryParams);

    /**
     * Cleans the cache associated with the specified service.
     * <p>
     * This method allows plugins to request the invalidation of cached data related to a specific service, 
     * ensuring fresh data is retrieved on subsequent requests.
     * 
     * @param serviceName a {@link String} representing the name of the service whose cache should be cleared.
     */
    void cleanCacheForService(final String serviceName);
}