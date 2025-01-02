package ai.learningsystems.gloobermkp.espproxies.plugin.shared.utils;

import static ai.learningsystems.gloobermkp.espproxies.plugin.shared.utils.HttpHeaderSharedConstants.SERVICEREF_NOT_SET_IN_HEADER;
import static ai.learningsystems.gloobermkp.espproxies.plugin.shared.utils.HttpHeaderSharedConstants.X_GMKP_XSP_SERVICE_REF;

import org.springframework.http.server.reactive.ServerHttpRequest;


/**
 * Utility class for extracting service-related properties from a {@link ServerHttpRequest}.
 * <p>
 * This class provides static methods to extract the service path and service reference
 * from the HTTP request. It ensures safe handling of null values and missing headers.
 */
public class HttpRequestPropertyProvider
{
    
    /**
     * Extracts the service path from the given {@link ServerHttpRequest}.
     * <p>
     * The service path is derived from the application-specific path within the request.
     *
     * @param request the HTTP request from which to extract the service path
     * @return the service path as a {@code String}
     * @throws IllegalArgumentException if the request is {@code null}
     */
    public static String getServicePath(final ServerHttpRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("ServerHttpRequest cannot be null.");
        }

        return request.getPath() //
                .pathWithinApplication() //
                .value();
    }
    
    
    /**
     * Extracts the service reference from the given {@link ServerHttpRequest}.
     * <p>
     * The service reference is typically provided in the HTTP header {@code X-GMKP-XSP-Service-Ref}.
     * If the header is missing or empty, a default value {@code "SERVICEREF_NOT_SET_IN_HEADER"} is returned.
     *
     * @param request the HTTP request from which to extract the service reference
     * @return the service reference as a {@code String}, or {@code "SERVICEREF_NOT_SET_IN_HEADER"} if not present
     * @throws IllegalArgumentException if the request is {@code null}
     */
    public static String getServiceRef(final ServerHttpRequest request) {
       
        if (request == null) {
            throw new IllegalArgumentException("ServerHttpRequest cannot be null.");
        }

        // Retrieve the service reference header values
        final var serviceRefHeaderValues = request.getHeaders().get(X_GMKP_XSP_SERVICE_REF);

        // Return the first value if present; otherwise, return the default
        if (serviceRefHeaderValues != null && !serviceRefHeaderValues.isEmpty()) {
            
            return serviceRefHeaderValues.get(0);
        }

        return SERVICEREF_NOT_SET_IN_HEADER;
    }
    
}

