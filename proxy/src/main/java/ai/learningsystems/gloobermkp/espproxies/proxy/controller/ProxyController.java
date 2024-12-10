package ai.learningsystems.gloobermkp.espproxies.proxy.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore.GenericProxyService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * REST controller for handling generic proxy requests.
 * On Gloober Market Place External Service Provider Services API URL are of the form : 
 *      http://GLOOBER_ESP_PREXIX/externalprovidername/native service URL
 * Example :
 * The native OpenAI Chat completion API URL is : https://api.openai.com/v1/chat/completions
 * it becomes on Gloober Market Place : https://api.gloober-mkp.ai/openai/v1/chat/completions 
 * 
 * Redirects requests to the appropriate service and returns the response with calculated usage metrics.
 */
@Slf4j
@RestController
@RequestMapping("/**")
public class ProxyController {

    private final GenericProxyService proxyService;
    
    
    @Autowired
    public ProxyController(final GenericProxyService proxyService) {
    
    	this.proxyService = proxyService;
    	log.info("ProxyController started !");
    }

    /**
     * Handles proxy requests, preserving the original request's HTTP method, headers, and parameters.
     *
     * @param headers     the HTTP headers of the incoming request.
     * @param queryParams the query parameters of the request.
     * @param requestBody the body of the incoming request (optional).
     * @param method      the HTTP method (GET, POST, etc.).
     * @param exchange    the ServerWebExchange for accessing request data.
     * @return a Mono wrapping the proxied response with usage metrics.
     */  
    @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public Flux<?> handleProxyRequest(
            @RequestHeader HttpHeaders headers,
            @RequestParam Map<String, String> queryParams,
            @RequestBody(required = false) String requestBody,
            HttpMethod method,
            ServerWebExchange exchange) {

        
        String fullEndpoint = extractFullEndpoint(exchange);
        log.debug("ProxyController receives full endpoint : " + fullEndpoint);
        
        final String trimedRequestBody = requestBody.trim();
        final boolean isStreamingRequested = proxyService.isStreamReplyRequested(fullEndpoint, 
                headers, trimedRequestBody, queryParams);
        
        if(isStreamingRequested) {
            return handleStreamRequest(fullEndpoint, method, headers, trimedRequestBody, queryParams);
       }
       return proxyService.proxyRequest(fullEndpoint, method, headers, trimedRequestBody, queryParams);
    
    }

    
    private Flux<ServerSentEvent<String>> handleStreamRequest(final String fullEndpoint, 
            final  HttpMethod method, 
            final HttpHeaders headers, 
            final String trimedRequestBody, 
            final @RequestParam Map<String, String> queryParams) {
        
        Flux<?> eventStream =  proxyService.proxyRequest(fullEndpoint, method, headers, trimedRequestBody, queryParams);
        
        @SuppressWarnings("unchecked")
        Flux<ServerSentEvent<String>> sseStream = eventStream instanceof Flux
              ? (Flux<ServerSentEvent<String>>) eventStream
              : Flux.error(new IllegalStateException("Expected Flux<ServerSentEvent<String>>"));

        Flux<ServerSentEvent<String>> processedSseStream = sseStream
                .doOnNext(content -> {
                    log.debug("data: name[{}], id [{}], content[{}]",content.event(), content.id(), content.data());
                })
                .doOnError(error -> {
                    log.error("Error receiving SSE: {}", error);
                })
                .doOnComplete(() -> {
                    log.debug("Completed receiving SSE events.");
                });

        return processedSseStream;
        
    }
    
    /**
     * Extracts the ESP and service path from the full request path after "/gloober-mkp/".
     *
     * @param exchange the ServerWebExchange from the client
     * @return the full path to be used as endpoint, e.g., "openai/v1/chat/completions"
     */
    private String extractFullEndpoint(ServerWebExchange exchange) {
        String fullPath = exchange.getRequest().getURI().getRawPath();
        return fullPath;
    }
}
