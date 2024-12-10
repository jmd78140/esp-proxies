package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import reactor.core.publisher.Flux;


/**
 * Interface for handling HTTP requests in a proxy service.
 * <p>
 * This interface defines methods for setting up a proxy service, determining whether
 * a streamed response is requested, handling requests, and populating custom HTTP headers
 * specific to the service protocol.
 */
public interface IRequestHandler {

   

    /**
     * Determines if a streamed reply is requested based on the provided HTTP headers,
     * request body, and query parameters.
     * 
     * @param headers a {@link HttpHeaders} object representing the HTTP headers of the request.
     * @param requestBody a {@link String} representing the body of the HTTP request.
     * @param queryParams a {@link Map} containing query parameters as key-value pairs.
     * @return {@code true} if a streamed reply is requested; {@code false} otherwise.
     */
    boolean isStreamReplyRequested(final HttpHeaders headers,
                                   final String requestBody, 
                                   final Map<String, String> queryParams);

    /**
     * Handles the given HTTP request by forwarding it to the specified endpoint.
     * <p>
     * This method processes the request and returns a reactive {@link Flux} representing
     * the response. The implementation should ensure that the request is correctly proxied
     * and any required transformations are applied.
     * 
     * @param endpoint the {@link URI} of the target endpoint.
     * @param method the {@link HttpMethod} representing the HTTP method of the request (e.g., GET, POST).
     * @param headers a {@link HttpHeaders} object representing the HTTP headers of the request.
     * @param requestBody a {@link String} representing the body of the HTTP request.
     * @param queryParams a {@link Map} containing query parameters as key-value pairs.
     * @return a {@link Flux} representing the response to the request.
     */
    Flux<?> handleRequest(final URI endpoint, 
                          final HttpMethod method, 
                          final HttpHeaders headers, 
                          final String requestBody, 
                          final Map<String, String> queryParams);

    /**
     * Populates the given HTTP headers with any custom fields required by the target service protocol.
     * <p>
     * Implementations may modify the headers in place to add protocol-specific fields. If no custom
     * headers are needed, this method can simply return the provided {@link HttpHeaders} unchanged.
     * 
     * @param headers a {@link HttpHeaders} object representing the original HTTP headers.
     * @return the modified {@link HttpHeaders} object, potentially including additional custom headers.
     */
    HttpHeaders populateRequestCustomHeaders(final HttpHeaders headers);
}