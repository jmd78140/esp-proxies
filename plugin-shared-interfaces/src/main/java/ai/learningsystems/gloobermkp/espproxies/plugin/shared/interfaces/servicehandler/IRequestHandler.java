package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler;


import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import reactor.core.publisher.Flux;



/**
 * Interface for handling HTTP requests in a proxy service.
 * <p>
 * This interface defines methods for setting up a proxy service, determining
 * whether a streamed response is requested, handling requests, and populating
 * custom HTTP headers specific to the service protocol.
 */
public interface IRequestHandler
{


    /**
     * Determines if a streamed reply is requested based on the provided HTTP
     * headers, request body, and query parameters.
     * 
     * @param request     the incoming {@link ServerHttpRequest} to handle
     * @param requestBody a {@link String} representing the body of the HTTP
     *                    request.
     * @return {@code true} if a streamed reply is requested; {@code false}
     *         otherwise.
     */
    boolean isStreamReplyRequested(final ServerHttpRequest request, final String requestBody);


    /**
     * Handles the given HTTP request by applying provider-specific modifications
     * and forwarding it to the proxy for execution.
     * <p>
     * This method allows for service-specific customizations of the HTTP request,
     * such as modifying headers, query parameters, or the request body. After
     * applying these modifications, the method delegates the execution of the
     * request to the appropriate proxy methods for standard or streaming requests.
     * </p>
     * 
     * @param retargetedRequest the {@link ServerHttpRequest} representing the
     *                          retargeted HTTP request. This includes the updated
     *                          URI, method, headers, and query parameters.
     * @param requestBody       a {@link String} containing the body of the HTTP
     *                          request, or {@code null} if the request does not
     *                          require a body.
     * @return a {@link Flux} representing the response to the request, either as a
     *         standard or streaming response. The exact behavior depends on the
     *         provider-specific logic implemented.
     * @throws RuntimeException if an error occurs during the modification or
     *                          forwarding of the request.
     */
    Flux<?> handleRequest(final ServerHttpRequest retargetedRequest, final String requestBody);


    /**
     * Populates the given HTTP headers with any custom fields required by the
     * target service protocol.
     * <p>
     * Implementations may modify the headers in place to add protocol-specific
     * fields. If no custom headers are needed, this method can simply return the
     * provided {@link HttpHeaders} unchanged.
     * 
     * @param headers a {@link HttpHeaders} object representing the original HTTP
     *                headers.
     * @return the modified {@link HttpHeaders} object, potentially including
     *         additional custom headers.
     */
    HttpHeaders populateRequestCustomHeaders(final HttpHeaders headers);

}