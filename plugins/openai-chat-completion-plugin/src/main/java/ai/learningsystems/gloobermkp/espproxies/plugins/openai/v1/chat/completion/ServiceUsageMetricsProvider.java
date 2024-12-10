package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage.UsageMetrics;


public class ServiceUsageMetricsProvider
{
    private static Logger log = LoggerFactory.getLogger(OpenAIChatCompletionServiceHandler.class);
    
    public ServiceUsageMetricsProvider() {
        
    }
    
    
    /**
     * Retrieves the total tokens count and build the UsageMetrics map<name, usageMetric>
     * OpenAI total_tokens count for non streamed reply is in the JSon
     * reply body field : replybody.usage.total_tokens
     */
    public static UsageMetrics getMetrics(final String requestBody, final ResponseEntity<String> response)
    {
        final StandardRequestUsageMetricsProvider metricsProvider = new StandardRequestUsageMetricsProvider(requestBody, response);
        return metricsProvider.getMetrics();
    }
    
    
    /**
     * Count tokens in prompt request and list of chunks replies, sum them and build
     * the UsageMetrics map <name, usageMetric>
     */
    public static UsageMetrics getStreamMetrics(final String requestBody, List<String> responseChunks)
    {
        final StreamRequestUsageMetricsProvider metricsProvider = new StreamRequestUsageMetricsProvider(requestBody, responseChunks);
        return metricsProvider.getMetrics();
    }
    
}
