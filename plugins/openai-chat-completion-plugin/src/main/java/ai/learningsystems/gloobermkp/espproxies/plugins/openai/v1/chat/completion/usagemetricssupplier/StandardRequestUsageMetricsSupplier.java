package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.usagemetricssupplier;


import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.learningsystems.gloobermkp.external.commons.domains.llm.ELLMEngineModel;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.ServiceLLMModelUSageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.TokenCountUsageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.UsageMetrics;



public class StandardRequestUsageMetricsSupplier {

    @SuppressWarnings("unused")
    private final ServerHttpRequest request;
    private final String requestBody;
    private final ResponseEntity<String> response;

    
    public StandardRequestUsageMetricsSupplier(final ServerHttpRequest request, final String requestBody,
            final ResponseEntity<String> response) {

        this.request = request;
        this.requestBody = requestBody;
        this.response = response;
    }

    /**
     * Retrieves the total tokens count and build the UsageMetrics map<name, usageMetric>
     * OpenAI total_tokens count for non-streamed reply is in the JSON
     * reply body field: replybody.usage.total_tokens
     */
    public UsageMetrics getMetrics() {
        final ServiceLLMModelUSageMetric modelUsageMetric = extractLLMModel();
        final TokenCountUsageMetric totalTokenUsageMetric = extractTotalTokens();
        final UsageMetrics metrics = new UsageMetrics();

        metrics.addMetric(totalTokenUsageMetric.getName(), totalTokenUsageMetric);
        metrics.addMetric(modelUsageMetric.getName(), modelUsageMetric);

        return metrics;
    }

    private ServiceLLMModelUSageMetric extractLLMModel() {
        
        final RequestBodyModelExtractor requestModelExtractor = new RequestBodyModelExtractor(requestBody);
        final ELLMEngineModel model = requestModelExtractor.getLLMModel();
        return new ServiceLLMModelUSageMetric(model);
    }

    /**
     * Extract the token count from the standard reply body.
     */
    private TokenCountUsageMetric extractTotalTokens() {
        
        try {
            final String responseBody = response.getBody();
            final ObjectMapper objectMapper = new ObjectMapper();
            final JsonNode parsedResponseBody = objectMapper.readTree(responseBody);
            final JsonNode usageNode = parsedResponseBody.path("usage");
            final JsonNode totalTokenNode = usageNode.path("total_tokens");
            final int totalToken = totalTokenNode.asInt();
            return new TokenCountUsageMetric(totalToken);
        } 
        catch (Exception exception) {
            throw new RuntimeException("Error extracting metrics from response", exception);
        }
    }

}
