package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.usagemetricssupplier;


import java.util.List;

import org.springframework.http.server.reactive.ServerHttpRequest;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.utils.HttpRequestPropertyProvider;
import ai.learningsystems.gloobermkp.external.commons.domains.llm.ELLMEngineModel;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.ReplyTokenCountUsageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.RequestTokenCountUsageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.ServiceLLMModelUSageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.TotalTokenCountUsageMetric;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.UsageMetrics;



public class StreamRequestUsageMetricsSupplier
{

    private final ServerHttpRequest          request;
    private final String                     requestBody;
    private final StreamRequestTokenCounter  streamRequestTokenCounter;
    private final StreamResponseTokenCounter streamResponseTokenCounter;


    public StreamRequestUsageMetricsSupplier(final ServerHttpRequest request, final String requestBody,
            List<String> responseChunks) {

        this.request               = request;
        this.requestBody           = requestBody;
        streamRequestTokenCounter  = new StreamRequestTokenCounter(requestBody);
        streamResponseTokenCounter = new StreamResponseTokenCounter(responseChunks);
    }


    /**
     * Count tokens in prompt request and list of chunks replies, sum them and build
     * the UsageMetrics map <name, usageMetric>
     */
    public UsageMetrics getMetrics()
    {

        final String                     servicePath      = HttpRequestPropertyProvider.getServicePath(request);
        final String                     serviceRef       = HttpRequestPropertyProvider.getServiceRef(request);
        final ServiceLLMModelUSageMetric modelUsageMetric = extractRequestLLMModel();
        final int                        tokensInRequest  = streamRequestTokenCounter.count();
        final int                        tokensInResponse = streamResponseTokenCounter.count();
        final int                        totalToken       = tokensInRequest + tokensInResponse;
        final UsageMetrics               metrics          = new UsageMetrics();
        metrics.addMetricToComponent(servicePath, serviceRef, new RequestTokenCountUsageMetric(tokensInRequest));
        metrics.addMetricToComponent(servicePath, serviceRef, new ReplyTokenCountUsageMetric(tokensInResponse));
        metrics.addMetricToComponent(servicePath, serviceRef, new TotalTokenCountUsageMetric(totalToken));
        metrics.addMetricToComponent(servicePath, serviceRef, modelUsageMetric);

        return metrics;
    }


    private ServiceLLMModelUSageMetric extractRequestLLMModel()
    {

        final RequestBodyModelExtractor requestModelExtractor = new RequestBodyModelExtractor(requestBody);
        final ELLMEngineModel           model                 = requestModelExtractor.getLLMModel();
        return new ServiceLLMModelUSageMetric(model);
    }

}
