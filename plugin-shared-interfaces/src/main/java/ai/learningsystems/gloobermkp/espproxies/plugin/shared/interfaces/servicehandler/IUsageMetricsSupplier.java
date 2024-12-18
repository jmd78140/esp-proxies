package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler;


import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;

import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.UsageMetrics;



/**
 * Interface for calculating and supplying usage metrics for HTTP requests and responses.
 * <p>
 * Implementing this interface allows a plugin to provide detailed usage metrics
 * for both standard responses and streamed responses (e.g., Server-Sent Events - SSE).
 * These metrics can be used to track request and response characteristics such as
 * payload size, token usage, or other provider-specific metrics.
 * </p>
 */
public interface IUsageMetricsSupplier {

    /**
     * Computes usage metrics for standard HTTP responses.
     * <p>
     * This method calculates usage metrics based on the provided request data, request body,
     * and the HTTP response. These metrics can include the number of tokens used, payload size,
     * or other custom metrics specific to the service provider.
     * </p>
     *
     * @param request      the {@link org.springframework.http.server.reactive.ServerHttpRequest}
     *                     representing the HTTP request.
     * @param requestBody  a {@link String} containing the body of the HTTP request.
     * @param response     the {@link ResponseEntity} containing the HTTP response, including
     *                     headers, status, and body.
     * @return a {@link UsageMetrics} object containing the computed metrics.
     */
    public UsageMetrics getMetrics(final ServerHttpRequest request, final String requestBody,
                                   final ResponseEntity<String> response);

    /**
     * Computes usage metrics for streamed responses (e.g., Server-Sent Events).
     * <p>
     * This method calculates usage metrics based on the provided request data, request body,
     * and the chunks of the streamed response. These metrics are particularly useful for tracking
     * token usage or other incremental metrics generated during streaming.
     * </p>
     *
     * @param request        the {@link org.springframework.http.server.reactive.ServerHttpRequest}
     *                       representing the HTTP request.
     * @param requestBody    a {@link String} containing the body of the HTTP request.
     * @param responseChunks a {@link List} of {@link String}, where each entry represents a chunk
     *                       of the streamed response.
     * @return a {@link UsageMetrics} object containing the computed metrics.
     */
    public UsageMetrics getMetrics(final ServerHttpRequest request, final String requestBody,
                                   List<String> responseChunks);

}
