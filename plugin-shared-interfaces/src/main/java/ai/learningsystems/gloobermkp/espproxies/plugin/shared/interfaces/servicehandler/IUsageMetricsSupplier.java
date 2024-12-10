package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;

import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage.UsageMetric;
import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage.UsageMetrics;


/**
 * Interface for supplying usage metrics for requests and responses. 
 * Implementing this interface allows a plugin to provide detailed usage metrics 
 * for both standard and streamed responses (e.g., Server-Sent Events - SSE).
 */
public interface IUsageMetricsSupplier {

    /**
     * Computes usage metrics based on the given request and response data.
     * 
     * @param requestBody a {@link String} representing the body of the request.
     * @param response a {@link ResponseEntity} representing the response.
     * @return a {@link Map} where the key is the metric name and the value is the corresponding {@link UsageMetric}.
     */
    UsageMetrics getMetrics(final String requestBody, final ResponseEntity<String> response);

    
    /**
     * Computes usage metrics for streamed replies, such as Server-Sent Events (SSE),
     * based on the given request data and list of response chunks.
     * 
     * @param requestBody a {@link String} representing the body of the request.
     * @param responseChunks a {@link List} of {@link String}, each representing a chunk of the streamed response.
     * @return a {@link Map} where the key is the metric name and the value is the corresponding {@link UsageMetric}.
     */
    UsageMetrics getStreamMetrics(final String requestBody, List<String> responseChunks);

}
