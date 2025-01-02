package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.proxy.IProxyService;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandler;
import ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.endofstreamdetector.EndOfStreamDetector;
import ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.usagemetricssupplier.ServiceUsageMetricsSupplier;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.UsageMetrics;
import ai.learningsystems.gloobermkp.external.commons.domains.web.RequestModifier;
import reactor.core.publisher.Flux;


public class OpenAIChatCompletionServiceHandler implements IServiceHandler
{
    @SuppressWarnings("unused")
    private static Logger log = LoggerFactory.getLogger(OpenAIChatCompletionServiceHandler.class);
    
    private static String REQ_BODY_STREAM_OPTION_FIELD = "stream";
    
    final private IProxyService proxyService;
    private ObjectMapper  objectMapper = new ObjectMapper();
    
    
    public OpenAIChatCompletionServiceHandler(IProxyService proxyService)
    {
        this.proxyService = proxyService;   
    }

    
    /**
     * Read the stream field value of JSON request body
     * 
    * @param request the incoming {@link ServerHttpRequest} to handle
     * @param requestBody a {@link String} representing the body of the HTTP request.
     * @return {@code true} if the value of stream field of requestBody is {@code true};
     * defaults to {@code false} if "stream" is not present or an error occurs
     */
    @Override
    public boolean isStreamReplyRequested(final ServerHttpRequest request, final String requestBody)
    {
        return isStreamReplyRequested(requestBody);
    }

    
    
    /**
     * This methid allow request and reply customization before callbacking proxyService 
     * to execute the request in the correponding mode.
     * 
     */
    @Override
    public Flux<?> handleRequest(final ServerHttpRequest retargetedRequest, final String requestBody)
    {

        ServerHttpRequest modifiedRequest = RequestModifier.modifyHeaders(retargetedRequest, headers -> {
            HttpHeaders customHeaders = populateRequestCustomHeaders(headers);
            // Clear previous headers data
            headers.clear(); 
            headers.addAll(customHeaders);
        });
    
        if (isStreamReplyRequested(requestBody)) {
             return proxyService.executeStreamingRequest(modifiedRequest, requestBody);
        }
        
        return proxyService.executeRequest(modifiedRequest, requestBody).flux();
    }

  
    
    @Override
    public HttpHeaders populateRequestCustomHeaders(final HttpHeaders headers)
    {

        final HttpHeaders customHeaders = new HttpHeaders();

        customHeaders.setAccept(headers.getAccept());
        customHeaders.setContentType(headers.getContentType());
        customHeaders.setContentLength(headers.getContentLength());
        // Add provider specific header fields as needed
        // customHeaders.add("X-Custom-Header", "CustomValue");

        if (headers.containsKey("Authorization")) {
            customHeaders.add("Authorization", headers.getFirst("Authorization"));
        }
        
        return customHeaders;
    }

   
    
    @Override
    public UsageMetrics getMetrics(final ServerHttpRequest request, final String requestBody, final ResponseEntity<String> response)
    {

        return ServiceUsageMetricsSupplier.getMetrics(request, requestBody, response);
    }

    
    @Override
    public UsageMetrics getMetrics(final ServerHttpRequest request, final String requestBody, List<String> responseChunks)
    {
        
        return ServiceUsageMetricsSupplier.getMetrics(request, requestBody, responseChunks);
    }

    
    @Override
    public boolean isEndOfStream(String chunk)
    {
        
        return EndOfStreamDetector.isEndOfStream(chunk);
    }


    @Override
    public String getEndOfStreamMarker() 
    {
        return EndOfStreamDetector.getEndOfStreamMarker();
    }
    
    
    private boolean isStreamReplyRequested(final String requestBody)
    {
        try {
            JsonNode     rootNode     = objectMapper.readTree(requestBody);
            if (rootNode.has(REQ_BODY_STREAM_OPTION_FIELD)) {
                return rootNode.get(REQ_BODY_STREAM_OPTION_FIELD).asBoolean();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


   
   
}
