package ai.learningsystems.gloobermkp.espproxies.proxy.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore.GenericProxyService;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;



/**
 * REST controller for handling generic proxy requests. On Gloober Market Place
 * External Service Provider Services API URL are of the form :
 * http://GLOOBER_ESP_PREXIX/externalprovidername/native service URL Example :
 * The native OpenAI Chat completion API URL is :
 * https://api.openai.com/v1/chat/completions it becomes on Gloober Market Place
 * : https://api.gloober-mkp.ai/openai/v1/chat/completions
 * 
 * Redirects requests to the appropriate service and returns the response with
 * calculated usage metrics.
 */
@Slf4j
@RestController
@RequestMapping("/**")
public class ProxyController
{

    private final GenericProxyService proxyService;


    @Autowired
    public ProxyController(final GenericProxyService proxyService) {

        this.proxyService = proxyService;
        log.info("ProxyController started !");
    }


    /**
     * Handles proxy requests, preserving the original request's HTTP method,
     * headers, and parameters.
     *
     * @param exchange    the ServerWebExchange for accessing request data.
     * @param requestBody the body of the incoming request (optional).<br>
     *                    Nota : Here, I choose to retrieve the body of the request
     *                    along with the request itself because retrieving it later
     *                    in the processing chain leads to rather inelegant handling
     *                    of {@link Flux<DataBuffer>}
     * @return a Mono wrapping the proxied response with usage metrics.
     * 
     * 
     */
    @RequestMapping(method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
    public Flux<?> handleProxyRequest(final ServerWebExchange exchange,
            @RequestBody(required = false) final String requestBody)
    {

        // Here we receive the requestBody
        final ServerHttpRequest request              = exchange.getRequest();
        final boolean           isStreamingRequested = proxyService.isStreamReplyRequested(request, requestBody);

        log.debug(
                "proxyRequest() received request - fullEndpoint: {}, headers: {}, method: {}, queryParams: {}, "
                        + " streaming reply requested : {}, requestBody: {}",
                request.getURI(), request.getHeaders(), request.getMethod(),
                request.getQueryParams().toSingleValueMap(), isStreamingRequested,
                requestBody != null ? requestBody : "No body");

        if (isStreamingRequested) {
            return handleStreamRequest(request, requestBody);
        }
        return proxyService.proxyRequest(request, requestBody);

    }


    private Flux<ServerSentEvent<String>> handleStreamRequest(final ServerHttpRequest request, final String requestBody)
    {

        Flux<?> eventStream = proxyService.proxyRequest(request, requestBody);

        @SuppressWarnings("unchecked")
        Flux<ServerSentEvent<String>> sseStream = eventStream instanceof Flux
                ? (Flux<ServerSentEvent<String>>) eventStream
                : Flux.error(new IllegalStateException("Expected Flux<ServerSentEvent<String>>"));

        Flux<ServerSentEvent<String>> processedSseStream = sseStream.doOnNext(content -> {
            log.debug("data: name[{}], id [{}], content[{}]", content.event(), content.id(), content.data());
        }).doOnError(error -> {
            log.error("Error receiving SSE: {}", error);
        }).doOnComplete(() -> {
            log.debug("Completed receiving SSE events.");
        });

        return processedSseStream;

    }

}
