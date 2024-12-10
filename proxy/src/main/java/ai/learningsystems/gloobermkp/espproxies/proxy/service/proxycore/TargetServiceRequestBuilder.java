package ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestBodySpec;
import org.springframework.web.reactive.function.client.WebClient.RequestBodyUriSpec;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TargetServiceRequestBuilder
{
    private final WebClient client;
    private final URI endpoint; 
    private final HttpMethod method;
    private final HttpHeaders headers; 
    private final String requestBody; 
    private final Map<String, String> queryParams;
    
    
    public TargetServiceRequestBuilder( final WebClient client,
            final URI endpoint, 
            final HttpMethod method, 
            final HttpHeaders headers, 
            final String requestBody,
            Map<String, String> queryParams) 
    {

        this.client      = client; 
        this.endpoint    = endpoint;
        this.method      = method;
        this.headers     = headers;
        this.requestBody = requestBody;
        this.queryParams = queryParams;
    }

    
    public RequestBodySpec buildRequest() {
        
        final RequestBodyUriSpec requestWithMethod = populateMethod();
        final RequestBodySpec requestWithEndPointAndParams = populateEndPointUriWithParams(requestWithMethod);
        final RequestBodySpec requestWithHeaders = populateHeader(requestWithEndPointAndParams);
        final RequestBodySpec requestWithBody = populateBody(requestWithHeaders);
        
        return requestWithBody;
    }
    
    
    private RequestBodyUriSpec populateMethod()  {
        
        return client.method(method);    
    }
    
    
    private RequestBodySpec populateEndPointUriWithParams(final RequestBodyUriSpec requestSpec) {
        
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(endpoint);
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(uriBuilder::queryParam); // Add query parameters to the URI
        }
        
        return requestSpec.uri(uriBuilder.toUriString());
    }
    
    
    private RequestBodySpec populateHeader(final RequestBodySpec requestSpec) {
        
        if (headers != null) {
            headers.forEach((key, values) -> {
                values.forEach(value -> {
                    // Log headers being added
                    log.debug("Adding Header: {}: {}", key, value);
                    requestSpec.header(key, value);
                });
            });
        }
        
        return requestSpec;
    }

    
    private RequestBodySpec populateBody(final RequestBodySpec requestSpec) {
        
        requestSpec.bodyValue(requestBody);
        return requestSpec;
    }
    
}
