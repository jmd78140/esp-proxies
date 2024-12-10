package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry;


import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.exception.ServiceHandlerConfigFileLoadException;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.exception.ServiceHandlerConfigFileParseException;



/**
 * Represents the configuration for a service handler, including its properties,
 * circuit breaker settings, and rate limiter settings. Provides functionality
 * to serialize the configuration to JSON and load it from YAML.
 */
public class ServiceHandlerConfiguration
{
    
    /**
     * Holds the properties of a service, such as name, target service base URL and endpoints, custom
     * headers, and other related configurations.
     */
    private ServiceProperties serviceProperties;


    /**
     * Default constructor for creating an empty ServiceHandlerConfiguration
     * instance.
     */
    public ServiceHandlerConfiguration() {

    }


    /**
     * Gets the service properties.
     * 
     * @return an instance of {@link ServiceProperties} containing the service
     *         configuration.
     */
    public ServiceProperties getServiceProperties()
    {

        return serviceProperties;
    }


    /**
     * Sets the service properties.
     * 
     * @param serviceProperties an instance of {@link ServiceProperties} to set.
     */
    public void setServiceProperties(final ServiceProperties serviceProperties)
    {

        this.serviceProperties = serviceProperties;
    }


    /**
     * Converts the configuration into a JSON string representation.
     * 
     * @return a JSON string representing this configuration.
     * @throws RuntimeException if an error occurs during serialization.
     */
    public String toJson()
    {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting ServiceConfiguration to JSON", e);
        }
    }


   
    /**
     * Loads a ServiceHandlerConfiguration instance from a YAML file.
     * 
     * @param filePath the path to the YAML file.
     * @return a {@link ServiceHandlerConfiguration} instance loaded from the file.
     * @throws RuntimeException if an error occurs while reading or parsing the YAML file.                      
     */ 
    public static ServiceHandlerConfiguration loadFromYaml(final Path configFilePath)
    {
        if (Files.notExists(configFilePath)) {
            throw new IllegalArgumentException("No such file or directory : " + configFilePath);
        }

        if (!Files.isRegularFile(configFilePath)) {
            throw new IllegalArgumentException("Not a file : " + configFilePath);
        }
        
        try (InputStream inputStream = Files.newInputStream(configFilePath)) {
           return parseYaml(inputStream);
        }
        catch (IOException ioException) {
            throw new ServiceHandlerConfigFileLoadException("Error loading YAML service handler configuration : \n" + ioException);
        } 
    }
    
    
    
    /**
     * Loads a ServiceHandlerConfiguration instance from a YAML input stream.
     * 
     * @param inputStream an {@link InputStream} containing the YAML data.
     * @return a {@link ServiceHandlerConfiguration} instance loaded from the input
     *         stream.
     * @throws RuntimeException if an error occurs while parsing the YAML data.
     */
    public static ServiceHandlerConfiguration parseYaml(final InputStream inputStream)
    {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            return mapper.readValue(inputStream, ServiceHandlerConfiguration.class);
        }
        catch (IOException ioException) {
            throw new ServiceHandlerConfigFileParseException("Error parsing YAML service handler configuration : \n" + ioException);
        }
    }


    /**
     * Represents the properties of a service, including its name, base URL, custom
     * headers, and configuration for circuit breakers and rate limiters.
     */
    public static class ServiceProperties
    {
        
        private String                      serviceName;
        private String                      targetServiceBaseUrl;
        private String                      targetServiceEndPoint;
        private Map<String, String>         customHeaders;
        private CircuitBreakerConfiguration circuitBreakerConfiguration;
        private RateLimiterConfiguration    rateLimiterConfiguration;


        /** Default constructor for ServiceProperties. */
        public ServiceProperties() {

        }


        /** @return the name of the service. */
        public String getServiceName()
        {

            return serviceName;
        }


        /** @param serviceName the name of the service to set. */
        public void setServiceName(final String serviceName)
        {

            this.serviceName = serviceName;
        }


        /** @return the base URL of the target service. */
        public String getTargetServiceBaseUrl()
        {

            return targetServiceBaseUrl;
        }


        /** @param targetServiceBaseUrl the base URL of the target service to set. */
        public void setTargetServiceBaseUrl(final String targetServiceBaseUrl)
        {

            this.targetServiceBaseUrl = targetServiceBaseUrl;
        }


        /** @return the endpoint of the target service. */
        public String getTargetServiceEndPoint()
        {

            return targetServiceEndPoint;
        }


        /** @param serviceEndPoint the endpoint of the service to set. */
        public void setTargetServiceEndPoint(final String targetServiceEndPoint)
        {

            this.targetServiceEndPoint = targetServiceEndPoint;
        }


        /** @return a map of custom headers for the service. */
        public Map<String, String> getCustomHeaders()
        {

            return customHeaders;
        }


        /** @param customHeaders a map of custom headers to set. */
        public void setCustomHeaders(final Map<String, String> customHeaders)
        {

            this.customHeaders = customHeaders;
        }


        /** @return the circuit breaker configuration. */
        public CircuitBreakerConfiguration getCircuitBreakerConfiguration()
        {

            return circuitBreakerConfiguration;
        }


        /**
         * @param circuitBreakerConfiguration the circuit breaker configuration to set.
         */
        public void setCircuitBreakerConfiguration(final CircuitBreakerConfiguration circuitBreakerConfiguration)
        {

            this.circuitBreakerConfiguration = circuitBreakerConfiguration;
        }

        
        /** @return the rate limiter configuration. */
        public RateLimiterConfiguration getRateLimiterConfiguration()
        {

            return rateLimiterConfiguration;
        }


        /** @param rateLimiterConfiguration the rate limiter configuration to set. */
        public void setRateLimiterConfiguration(final RateLimiterConfiguration rateLimiterConfiguration)
        {

            this.rateLimiterConfiguration = rateLimiterConfiguration;
        }

        
        /**
         * Converts the service properties into a JSON string representation.
         * 
         * @return a JSON string representing the service properties.
         * @throws RuntimeException if an error occurs during serialization.
         */
        public String toJson()
        {

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(this);
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting ServiceProperties to JSON", e);
            }
        }

    }

    
    /**
     * Configuration class for a circuit breaker, used to define its behavior and thresholds
     * based on Resilience4j's circuit breaker pattern.
     * <p>
     * A circuit breaker is used to improve the stability and resilience of a system by monitoring
     * service calls and controlling their flow when failures are detected. This configuration
     * class allows customization of key parameters that define the circuit breaker's behavior.
     */
    public static class CircuitBreakerConfiguration
    {
        /** The size of the sliding window used for failure rate calculations. */
        private int    slidingWindowSize;
        /** The failure rate threshold (in percentage) that triggers the circuit breaker to open. */
        private int    failureRateThreshold;
        /** 
         * The duration (as a string, e.g., "30s") for which the circuit breaker stays in the open state
         * before transitioning to the half-open state. 
         */
        private String waitDurationInOpenState;
        /** 
         * The number of permitted calls in the half-open state before determining whether to close 
         * or reopen the circuit breaker.
         */
        private int    permittedNumberOfCallsInHalfOpenState;

        
        /** Default constructor. */
        public CircuitBreakerConfiguration() {

        }

        /**
         * Gets the size of the sliding window used for failure rate calculations.
         * 
         * @return the sliding window size.
         */
        public int getSlidingWindowSize()
        {

            return slidingWindowSize;
        }

        /**
         * Sets the size of the sliding window used for failure rate calculations.
         * <p>
         * In Resilience4j, the sliding window is used to track the number of calls
         * and their success/failure rates. A larger window provides a more stable measurement,
         * while a smaller window reacts more quickly to changes in the system's behavior.
         * 
         * @param slidingWindowSize the size of the sliding window to set.
         */
        public void setSlidingWindowSize(final int slidingWindowSize)
        {

            this.slidingWindowSize = slidingWindowSize;
        }


        /**
         * Gets the failure rate threshold that triggers the circuit breaker to open.
         * 
         * @return the failure rate threshold (in percentage).
         */
        public int getFailureRateThreshold()
        {

            return failureRateThreshold;
        }

        
        /**
         * Sets the failure rate threshold that triggers the circuit breaker to open.
         * <p>
         * In Resilience4j, the failure rate threshold is a percentage that determines 
         * when the circuit breaker transitions to the open state. For example, a value 
         * of 50 means the circuit breaker will open if 50% or more of the calls fail
         * within the sliding window.
         * 
         * @param failureRateThreshold the failure rate threshold to set (in percentage).
         */
        public void setFailureRateThreshold(final int failureRateThreshold)
        {

            this.failureRateThreshold = failureRateThreshold;
        }

        
        /**
         * Gets the duration for which the circuit breaker stays in the open state.
         * 
         * @return the duration as a string (e.g., "30s").
         */
        public String getWaitDurationInOpenState()
        {

            return waitDurationInOpenState;
        }

        
        /**
         * Sets the duration for which the circuit breaker stays in the open state.
         * <p>
         * In Resilience4j, the circuit breaker stays in the open state for this duration
         * before transitioning to the half-open state. During this time, no calls are allowed
         * to pass through. The duration should be specified as a string, such as "30s" for 30 seconds.
         * 
         * @param waitDurationInOpenState the duration to set (e.g., "30s").
         */
        public void setWaitDurationInOpenState(final String waitDurationInOpenState)
        {

            this.waitDurationInOpenState = waitDurationInOpenState;
        }


        /**
         * Gets the number of permitted calls in the half-open state.
         * 
         * @return the number of permitted calls in the half-open state.
         */
        public int getPermittedNumberOfCallsInHalfOpenState()
        {

            return permittedNumberOfCallsInHalfOpenState;
        }


        /**
         * Sets the number of permitted calls in the half-open state.
         * <p>
         * In Resilience4j, the half-open state allows a limited number of test calls to determine
         * if the circuit breaker should transition back to the closed state or reopen. This parameter
         * controls how many calls are allowed during this state.
         * 
         * @param permittedNumberOfCallsInHalfOpenState the number of calls to set.
         */
        public void setPermittedNumberOfCallsInHalfOpenState(final int permittedNumberOfCallsInHalfOpenState)
        {

            this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
        }

        
        /**
         * Converts the circuit breaker configuration into a JSON string representation.
         * 
         * @return a JSON string representing this configuration.
         * @throws RuntimeException if an error occurs during serialization.
         */
        public String toJson()
        {

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(this);
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting CircuitBreakerConfiguration to JSON", e);
            }
        }

    }

    
    /**
     * Configuration class for a rate limiter, used to define its behavior and thresholds
     * based on Resilience4j's rate limiter pattern.
     * <p>
     * A rate limiter controls the number of calls to a service or resource over a given time period, 
     * helping to prevent overload and ensuring stable operation under high traffic conditions. 
     * This class provides configurable parameters for fine-tuning the rate limiter's behavior.
     */
    public static class RateLimiterConfiguration
    {
        /** The maximum number of calls allowed during a defined period. */
        private int    limitForPeriod;
        
        /** 
         * The timeout duration (as a string, e.g., "10s") for waiting when the limit is exceeded.
         * If the timeout is exceeded, the call is denied.
         */
        private String timeoutDuration;
        
        /** 
         * The duration (as a string, e.g., "1s") after which the rate limiter's counter is refreshed 
         * and the limit is reset.
         */
        private String limitRefreshPeriod;

        
        /** Default constructor. */
        public RateLimiterConfiguration() {

        }

        
        /**
         * Gets the maximum number of calls allowed during the defined period.
         * 
         * @return the limit for the period.
         */
        public int getLimitForPeriod()
        {

            return limitForPeriod;
        }

        
        /**
         * Sets the maximum number of calls allowed during the defined period.
         * <p>
         * In Resilience4j, this parameter defines the maximum number of calls that can be made 
         * within the `limitRefreshPeriod`. Once this limit is reached, further calls are restricted 
         * until the period refreshes.
         * 
         * @param limitForPeriod the maximum number of calls to set.
         */
        public void setLimitForPeriod(final int limitForPeriod)
        {

            this.limitForPeriod = limitForPeriod;
        }


        /**
         * Gets the timeout duration for waiting when the limit is exceeded.
         * 
         * @return the timeout duration as a string (e.g., "10s").
         */
        public String getTimeoutDuration()
        {

            return timeoutDuration;
        }

        
        /**
         * Sets the timeout duration for waiting when the limit is exceeded.
         * <p>
         * In Resilience4j, this parameter specifies how long a call will wait before being denied 
         * when the rate limit has been exceeded. A shorter duration results in quicker failure, 
         * while a longer duration allows more time for the limit to refresh.
         * 
         * @param timeoutDuration the timeout duration to set (e.g., "10s").
         */
        public void setTimeoutDuration(final String timeoutDuration)
        {

            this.timeoutDuration = timeoutDuration;
        }


        /**
         * Gets the duration after which the rate limiter's counter is refreshed.
         * 
         * @return the refresh period as a string (e.g., "1s").
         */
        public String getLimitRefreshPeriod()
        {

            return limitRefreshPeriod;
        }


        /**
         * Sets the duration after which the rate limiter's counter is refreshed.
         * <p>
         * In Resilience4j, this parameter defines the interval at which the rate limiter resets 
         * its counter, allowing new calls to be made. For example, setting it to "1s" resets 
         * the counter every second.
         * 
         * @param limitRefreshPeriod the refresh period to set (e.g., "1s").
         */
        public void setLimitRefreshPeriod(final String limitRefreshPeriod)
        {

            this.limitRefreshPeriod = limitRefreshPeriod;
        }

        
        /**
         * Converts the rate limiter configuration into a JSON string representation.
         * 
         * @return a JSON string representing this configuration.
         * @throws RuntimeException if an error occurs during serialization.
         */
        public String toJson()
        {

            try {
                ObjectMapper objectMapper = new ObjectMapper();
                return objectMapper.writeValueAsString(this);
            }
            catch (JsonProcessingException e) {
                throw new RuntimeException("Error converting RateLimiterConfiguration to JSON", e);
            }
        }

    }


}
