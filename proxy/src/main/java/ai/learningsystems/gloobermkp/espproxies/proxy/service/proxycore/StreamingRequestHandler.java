package ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandler;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.technical.ResponseTime;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.technical.TechnicalMetrics;
import ai.learningsystems.gloobermkp.external.commons.domains.metrics.usage.UsageMetrics;
import ai.learningsystems.gloobermkp.external.commons.domains.web.HttpRequestBuilder;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;



@Slf4j
public class StreamingRequestHandler
{

    final WebClient                client;
    private final IServiceHandler  serviceHandler;
    private final TechnicalMetrics technicalMetrics;
    private long                   requestStartTime;
    private long                   requestEndTime;


    public StreamingRequestHandler(final WebClient client, final IServiceHandler serviceHandler) {

        this.client           = client;
        this.serviceHandler   = serviceHandler;
        this.technicalMetrics = new TechnicalMetrics();
    }


    public Flux<?> handle(final ServerHttpRequest retargetedRequest, final String requestBody,
            final CircuitBreaker circuitBreaker, final RateLimiter rateLimiter)
    {

        List<String> accumulatedChunks = Collections.synchronizedList(new ArrayList<>());

        // This indirection through ServiceHandler allow request customization on
        // providers constraints
        Flux<?> eventStream = serviceHandler.handleRequest(retargetedRequest, requestBody)
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RateLimiterOperator.of(rateLimiter));

        @SuppressWarnings("unchecked")
        Flux<ServerSentEvent<String>> sseStream = eventStream instanceof Flux
                ? (Flux<ServerSentEvent<String>>) eventStream
                : Flux.error(new IllegalStateException("Expected Flux<ServerSentEvent<String>>"));

        return sseStream.concatMap(content -> processSSEChunk(retargetedRequest, requestBody, content, accumulatedChunks))
                .doOnError(error -> log.error("Error receiving SSE: {}", error))
                .doOnTerminate(() -> log.debug("Completed receiving SSE events."));

    }


    /**
     * Executes a streaming HTTP request and returns the response as a Flux of
     * String.
     * 
     * @param endpoint    the target endpoint for the request.
     * @param method      the HTTP method (GET, POST, etc.).
     * @param headers     the HTTP headers for the request.
     * @param requestBody the body of the request.
     * @param queryParams query parameters to be included in the request.
     * @return a Flux wrapping the HTTP streaming response body.
     */
    public Flux<ServerSentEvent<String>> executeStreamingRequest(final ServerHttpRequest retargetedRequest,
            final String requestBody)
    {

        ParameterizedTypeReference<ServerSentEvent<String>> type = new ParameterizedTypeReference<ServerSentEvent<String>>() {
        };

        final HttpRequestBuilder requestBuilder = new HttpRequestBuilder(client, retargetedRequest, requestBody);

        RequestBodySpec requestSpec = requestBuilder.build();
        requestStartTime = System.currentTimeMillis();

        return requestSpec.accept(MediaType.TEXT_EVENT_STREAM).header(HttpHeaders.CACHE_CONTROL, "no-store") // Add
                                                                                                             // Cache-Control
                                                                                                             // header
                .retrieve().onStatus(statusCode -> statusCode.is4xxClientError() || statusCode.is5xxServerError(),
                        clientResponse -> {
                            // Log error response and return Mono.error
                            log.error("Received error response: {}", clientResponse.statusCode());
                            return Mono.error(
                                    new RuntimeException("Error response from server: " + clientResponse.statusCode()));
                        })
                .bodyToFlux(type).map(content -> {
                    return ServerSentEvent.builder(content.data()).event(content.event()).id(content.id()).build();
                }).doOnTerminate(() -> log.debug("Stream processing terminated."));
    }


    private Flux<ServerSentEvent<String>> processSSEChunk(final ServerHttpRequest request, final String requestBody,
            ServerSentEvent<String> content, List<String> accumulatedChunks)
    {

        synchronized (accumulatedChunks) {
            accumulatedChunks.add(content.data());
        }

        if (serviceHandler.isEndOfStream(content.data())) {
            log.debug("End Of SSE Stream detected!");
            final UsageMetrics streamMetrics     = serviceHandler.getMetrics(request, requestBody, accumulatedChunks);
            String             jsonStreamMetrics = streamMetrics.toJson();

            requestEndTime = System.currentTimeMillis();
            technicalMetrics.addMetric("StreamResponseTime", new ResponseTime(requestStartTime, requestEndTime));
            String jsonTechnicalMetrics = technicalMetrics.toJson();

            String finalChunk = "{\"type\": \"EndOfStreamChunk\", " + "\"nativeEOSMarker\": \"data: [DONE]\", "
                    + "\"X-UsageMetrics\": \"" + jsonStreamMetrics + "\", " + "\"X-TechnicalMetrics\": \""
                    + jsonTechnicalMetrics + "\"}";

            ServerSentEvent<String> modifiedEvent = ServerSentEvent.builder(finalChunk).event(content.event())
                    .id(content.id()).build();

            return Flux.just(modifiedEvent);
        }
        else {
            return Flux.just(content);
        }
    }


}
