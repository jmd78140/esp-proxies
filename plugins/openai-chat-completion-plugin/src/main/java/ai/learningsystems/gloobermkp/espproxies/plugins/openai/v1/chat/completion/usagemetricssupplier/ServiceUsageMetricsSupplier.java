package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.usagemetricssupplier;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;

import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.UsageMetrics;



public class ServiceUsageMetricsSupplier
{
    
    public ServiceUsageMetricsSupplier() {
        
    }
    
    
    /**
     * Retrieves the total tokens count and build the UsageMetrics map<name, usageMetric>
     * OpenAI total_tokens count for non streamed reply is in the JSon
     * reply body field : replybody.usage.total_tokens
     */
    public static UsageMetrics getMetrics(final ServerHttpRequest request, final String requestBody,
            final ResponseEntity<String> response)
    {
        final StandardRequestUsageMetricsSupplier metricsProvider = new StandardRequestUsageMetricsSupplier(request, requestBody, response);
        return metricsProvider.getMetrics();
    }
    
    
    /**
     * Count tokens in prompt request and list of chunks replies, sum them and build
     * the UsageMetrics map <name, usageMetric>
     */
    public static UsageMetrics getMetrics(final ServerHttpRequest request, final String requestBody,
            List<String> responseChunks)
    {
        final StreamRequestUsageMetricsProvider metricsProvider = new StreamRequestUsageMetricsProvider(request, requestBody, responseChunks);
        return metricsProvider.getMetrics();
    }
    
}
