package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.usagemetricssupplier;

import java.util.List;

import org.springframework.http.server.reactive.ServerHttpRequest;

import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.TokenCountUsageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.UsageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.UsageMetrics;


public class StreamRequestUsageMetricsProvider
{

    private final StreamRequestTokenCounter streamRequestTokenCounter;
    private final StreamResponseTokenCounter streamResponseTokenCounter;
    
    
    public StreamRequestUsageMetricsProvider(final ServerHttpRequest request, final String requestBody, 
            List<String> responseChunks) 
    {
        
        streamRequestTokenCounter = new StreamRequestTokenCounter(requestBody);
        streamResponseTokenCounter = new StreamResponseTokenCounter(responseChunks);
    }
    
    
    /**
     * Count tokens in prompt request and list of chunks replies, sum them and build
     * the UsageMetrics map <name, usageMetric>
     */
    public UsageMetrics getMetrics()
    {
        
        final int                      tokensInRequest  = streamRequestTokenCounter.count();
        final int                      tokensInResponse = streamResponseTokenCounter.count();
        final int                      totalToken       = tokensInRequest + tokensInResponse;
        final UsageMetrics metrics     = new UsageMetrics();
        final UsageMetric              tokenCountMetric = new TokenCountUsageMetric(totalToken);
        metrics.addMetric(tokenCountMetric.getName(), tokenCountMetric);

        return metrics;
    }
   
}
