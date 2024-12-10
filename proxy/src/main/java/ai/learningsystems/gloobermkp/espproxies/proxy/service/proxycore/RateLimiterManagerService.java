package ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration.RateLimiterConfiguration;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RateLimiterManagerService
{
    private final RateLimiterRegistry    rateLimiterRegistry;
    
    @Autowired
    public RateLimiterManagerService(final RateLimiterRegistry    rateLimiterRegistry) {
        
        this.rateLimiterRegistry = rateLimiterRegistry;
    }
    
    
    /**
     * Retrieves a RateLimiter instance from the registry or creates a new one if
     * not present.
     * 
     * @param serviceName the name of the external service.
     * @param properties  the service properties containing the RateLimiter
     *                    configuration.
     * @return a configured RateLimiter instance.
     */
    public RateLimiter getRateLimiter(final String serviceName,
            final RateLimiterConfiguration rateLimiterConfiguration)
    {

        // Retrieve RateLimiter instance from the registry using the service name
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(serviceName);

        // If the RateLimiter doesn't exist in the registry, create and register a new
        // one
        if (null != rateLimiter) 
            return rateLimiter;
        
        return createRateLimiter(serviceName, rateLimiterConfiguration);
    }


    /**
     * Removes a RateLimiter instance from the registry by service name.
     */
    public void removeRateLimiter(final String serviceName)
    {

        // Remove the RateLimiter from the registry using the service name
        rateLimiterRegistry.remove(serviceName);
    }
    
    
    private RateLimiter createRateLimiter(final String serviceName,
            final RateLimiterConfiguration rateLimiterConfiguration) {
     
        final RateLimiterConfig rateLimiterConfig = RateLimiterConfig.custom()
                .limitForPeriod(rateLimiterConfiguration.getLimitForPeriod())
                .timeoutDuration(Duration.parse(rateLimiterConfiguration.getTimeoutDuration()))
                .limitRefreshPeriod(Duration.parse(rateLimiterConfiguration.getLimitRefreshPeriod())).build();
        
        return rateLimiterRegistry.rateLimiter(serviceName, rateLimiterConfig);
    }

}
