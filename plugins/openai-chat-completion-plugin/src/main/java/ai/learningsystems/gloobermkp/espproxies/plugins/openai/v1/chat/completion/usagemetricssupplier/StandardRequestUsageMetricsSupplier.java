package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.usagemetricssupplier;


import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.utils.HttpRequestPropertyProvider;
import ai.learningsystems.gloobermkp.external.commons.domains.llm.ELLMEngineModel;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.ReplyTokenCountUsageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.RequestTokenCountUsageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.ServiceLLMModelUSageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.TotalTokenCountUsageMetric;
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
        
        final String servicePath = HttpRequestPropertyProvider.getServicePath(request);
        final String serviceRef  = HttpRequestPropertyProvider.getServiceRef(request);
        final ServiceLLMModelUSageMetric modelUsageMetric = extractRequestLLMModel();
        final OAITokenCounters tokensCounters = extractReplyTokensCounters();
        final UsageMetrics metrics = new UsageMetrics();

        metrics.addMetricToComponent(servicePath, serviceRef, new RequestTokenCountUsageMetric(tokensCounters.promptTokens()));
        metrics.addMetricToComponent(servicePath, serviceRef, new ReplyTokenCountUsageMetric(tokensCounters.completionTokens()));
        metrics.addMetricToComponent(servicePath, serviceRef, new TotalTokenCountUsageMetric(tokensCounters.totalTokens()));
        metrics.addMetricToComponent(servicePath, serviceRef, modelUsageMetric);

        return metrics;
    }

    
    private OAITokenCounters extractReplyTokensCounters() {
        
        final ReplyBodyTokenCountersExtractor replyBodyTokenCountersExtractor = new ReplyBodyTokenCountersExtractor(response.getBody());
        return replyBodyTokenCountersExtractor.extractTokenCounters();
    }
   
    
    private ServiceLLMModelUSageMetric extractRequestLLMModel() {
        
        final RequestBodyModelExtractor requestModelExtractor = new RequestBodyModelExtractor(requestBody);
        final ELLMEngineModel model = requestModelExtractor.getLLMModel();
        return new ServiceLLMModelUSageMetric(model);
    }
    
}
