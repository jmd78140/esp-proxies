package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.proxy;

import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpRequest;

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
     * Executes an HTTP request based on the provided retargeted request and request body.
     * <p>
     * This method sends the provided {@link ServerHttpRequest}, which has been retargeted with a new endpoint,
     * to the appropriate service. It includes the option to provide a request body for methods like POST or PUT,
     * and processes the response as a {@link Mono} containing the complete HTTP response.
     * </p>
     * 
     * @param retargetedRequest the {@link ServerHttpRequest} representing the retargeted HTTP request.
     *                          This includes the updated URI, method, headers, and query parameters.
     * @param requestBody a {@link String} representing the request body content if it exists null otherwise.                         
     * @return a {@link Mono} containing the {@link ResponseEntity<String>} with the response from the target service,
     *         including the status code, headers, and response body.
     * @throws IllegalArgumentException if the `retargetedRequest` is null.
     * @throws RuntimeException if an error occurs during request execution.
     */
    Mono<ResponseEntity<String>> executeRequest(final ServerHttpRequest retargetedRequest, final String requestBody);

    /**
    * Executes a streaming HTTP request based on the provided retargeted request and request body.
    * <p>
    * This method sends the provided {@link ServerHttpRequest}, which has been retargeted with a new endpoint,
    * to the appropriate service and processes the response as a stream of {@link ServerSentEvent}.
    * It supports streaming protocols such as Server-Sent Events (SSE) and processes each chunk of the response
    * as a separate event.
    * </p>
    * 
    * @param retargetedRequest the {@link ServerHttpRequest} representing the retargeted HTTP request.
    *                          This includes the updated URI, method, headers, and query parameters.
    * @param requestBody a {@link String} representing the request body content if it exists null otherwise.
    * @return a {@link Flux} of {@link ServerSentEvent<String>} representing the streamed response from 
    *         the target service. Each emitted event corresponds to a chunk of the response data.
    * @throws IllegalArgumentException if the `retargetedRequest` is null.
    * @throws RuntimeException if an error occurs during the streaming request execution.
    */
    public Flux<ServerSentEvent<String>> executeStreamingRequest(final ServerHttpRequest retargetedRequest, final String requestBody);

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