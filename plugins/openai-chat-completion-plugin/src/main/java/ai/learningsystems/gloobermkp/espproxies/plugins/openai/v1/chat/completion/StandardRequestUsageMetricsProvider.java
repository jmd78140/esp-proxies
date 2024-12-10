package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage.TokenCountUsageMetric;
import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage.UsageMetric;
import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage.UsageMetrics;



public class StandardRequestUsageMetricsProvider
{

    private static Logger log = LoggerFactory.getLogger(StandardRequestUsageMetricsProvider.class);
    
    @SuppressWarnings("unused")
    private final String                 requestBody;
    private final ResponseEntity<String> response;


    public StandardRequestUsageMetricsProvider(final String requestBody, final ResponseEntity<String> response) {

        this.requestBody = requestBody;
        this.response    = response;
    }


    /**
     * Retrieves the total tokens count and build the UsageMetrics map<name,
     * usageMetric> OpenAI total_tokens count for non streamed reply is in the JSon
     * reply body field : replybody.usage.total_tokens
     */
    public UsageMetrics getMetrics()
    {
        final int          totalToken       = extractTotalTokens();
        final UsageMetrics metrics          = new UsageMetrics();
        final UsageMetric  tokenCountMetric = new TokenCountUsageMetric(totalToken);
        metrics.addMetric(tokenCountMetric.getName(), tokenCountMetric);

        return metrics;
    }


    /**
     * Extact the token count from standard reply body
     * 
     * @param responseBody
     * @return
     */
    private int extractTotalTokens()
    {
        final String responseBody = response.getBody();
        try {
            final ObjectMapper objectMapper       = new ObjectMapper();
            final JsonNode     parsedResponseBody = objectMapper.readTree(responseBody);
            final JsonNode     usageNode          = parsedResponseBody.path("usage");
            final JsonNode     totalTokenNode     = usageNode.path("total_tokens");
            final int          totalToken         = totalTokenNode.asInt();
            return totalToken;
        }
        catch (Exception e) {
            throw new RuntimeException("Error extracting metrics from response", e);
        }
    }

}
