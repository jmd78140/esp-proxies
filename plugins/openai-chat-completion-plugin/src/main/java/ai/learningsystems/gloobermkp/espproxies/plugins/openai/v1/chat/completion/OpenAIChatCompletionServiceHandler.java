package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage.UsageMetrics;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.proxy.IProxyService;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandler;
import reactor.core.publisher.Flux;


public class OpenAIChatCompletionServiceHandler implements IServiceHandler
{
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(OpenAIChatCompletionServiceHandler.class);
    
    final private IProxyService proxyService;
    private ObjectMapper  objectMapper = new ObjectMapper();
    @SuppressWarnings("unused")
    private HttpHeaders   incomingHeadersBackup;

    
    public OpenAIChatCompletionServiceHandler(IProxyService proxyService)
    {
        this.proxyService = proxyService;   
    }

    
    /**
     * Read the stream field value of JSON request body
     * 
     * @param requestBody
     * @return the value of stream field, defaults to false if "stream" is not
     *         present or an error occurs
     */
    @Override
    public boolean isStreamReplyRequested(HttpHeaders headers, String requestBody, Map<String, String> queryParams)
    {
        try {
            JsonNode     rootNode     = objectMapper.readTree(requestBody);
            if (rootNode.has("stream")) {
                return rootNode.get("stream").asBoolean();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    
    /**
     * This methid allow request and reply customization before callbacking proxyService 
     * to execute the request in the correponding mode.
     * 
     */
    @Override
    public Flux<?> handleRequest(URI endpoint, HttpMethod method, HttpHeaders headers, String requestBody,
            Map<String, String> queryParams)
    {

        // Backup incoming headers for later user if needed and
        // populate custom headers before making the request
        incomingHeadersBackup = headers;
        final HttpHeaders customHeaders        = populateRequestCustomHeaders(headers);
        final boolean     isStreamingRequested = isStreamReplyRequested(headers, requestBody, queryParams);

        if (isStreamingRequested) {
             return proxyService.executeStreamingRequest(endpoint, method, customHeaders, requestBody, queryParams);
        }
        
        return proxyService.executeRequest(endpoint, method, customHeaders, requestBody, queryParams).flux();
    }

    
    @Override
    public HttpHeaders populateRequestCustomHeaders(HttpHeaders headers)
    {

        final HttpHeaders customHeaders = new HttpHeaders();
        customHeaders.setAccept(headers.getAccept());
        customHeaders.setContentType(headers.getContentType());
        customHeaders.add("Authorization", headers.get("Authorization").get(0));
        customHeaders.setContentLength(headers.getContentLength());
        return customHeaders;
    }

    
    @Override
    public UsageMetrics getMetrics(String requestBody, final ResponseEntity<String> response)
    {

        return ServiceUsageMetricsProvider.getMetrics(requestBody, response);
    }

    
    @Override
    public UsageMetrics getStreamMetrics(String requestBody, List<String> responseChunks)
    {
        
        return ServiceUsageMetricsProvider.getStreamMetrics(requestBody, responseChunks);
    }

    
    @Override
    public boolean isEndOfStream(String chunk)
    {
        
        return EndOfStreamDetector.isEndOfStream(chunk);
    }

}
