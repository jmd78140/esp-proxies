package ai.learningsystems.gloobermkp.espproxies.proxy.service.proxycore;


import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration.CircuitBreakerConfiguration;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.extern.slf4j.Slf4j;



@Service
@Slf4j
public class CircuitBreakerManagerService
{

    private final CircuitBreakerRegistry circuitBreakerRegistry;


    @Autowired
    public CircuitBreakerManagerService(final CircuitBreakerRegistry circuitBreakerRegistry) {

        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }


    /**
     * Retrieves a CircuitBreaker instance from the registry or creates a new one if
     * not present.
     * 
     * @param serviceName the name of the external service.
     * @param properties  the service properties containing the CircuitBreaker
     *                    configuration.
     * @return a configured CircuitBreaker instance.
     */
    public CircuitBreaker getCircuitBreaker(final String serviceName,
            final CircuitBreakerConfiguration circuitBreakerConfiguration)
    {

        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(serviceName);
       
        if (null != circuitBreaker)
            return circuitBreaker;
        
        return createCircuitBreaker(serviceName, circuitBreakerConfiguration); 
    }

    
    /**
     * Removes a CircuitBreaker instance from the registry by service name.
     */
    public void removeCircuitBreaker(final String serviceName)
    {

        // Remove the CircuitBreaker from the registry using the service name
        circuitBreakerRegistry.remove(serviceName);
    }

        
    private CircuitBreaker createCircuitBreaker(final String serviceName, 
            CircuitBreakerConfiguration circuitBreakerConfiguration) 
    {
        
        final CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(circuitBreakerConfiguration.getSlidingWindowSize())
                .failureRateThreshold(circuitBreakerConfiguration.getFailureRateThreshold())
                .waitDurationInOpenState(Duration.parse(circuitBreakerConfiguration.getWaitDurationInOpenState()))
                .permittedNumberOfCallsInHalfOpenState(
                        circuitBreakerConfiguration.getPermittedNumberOfCallsInHalfOpenState())
                .build();
        
        return circuitBreakerRegistry.circuitBreaker(serviceName, circuitBreakerConfig);
    }

}
