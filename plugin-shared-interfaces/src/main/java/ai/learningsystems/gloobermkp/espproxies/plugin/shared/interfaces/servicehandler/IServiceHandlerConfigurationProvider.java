package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.exception.ServiceHandlerConfigFileLoadException;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.exception.ServiceHandlerConfigFileParseException;

/**
 * Interface for providing the configuration and factory for a service handler.
 * <p>
 * This interface is used to supply both the {@link ServiceHandlerConfiguration}, 
 * which defines the settings and properties of a service handler, 
 * and the {@link IServiceHandlerFactory}, which is responsible for creating 
 * instances of the service handler.
 */
public interface IServiceHandlerConfigurationProvider {

    /**
     * Retrieves the configuration of the service handler.
     * <p>
     * The {@link ServiceHandlerConfiguration} contains the necessary properties 
     * and settings required for the service handler's operation, such as base URLs, 
     * custom headers, and circuit breaker or rate limiter configurations.
     * 
     * @return an instance of {@link ServiceHandlerConfiguration} representing the configuration of the service handler.
     */
    ServiceHandlerConfiguration getServiceHandlerConfiguration() 
            throws ServiceHandlerConfigFileParseException, ServiceHandlerConfigFileLoadException;

    /**
     * Retrieves the factory for creating instances of the service handler.
     * <p>
     * The {@link IServiceHandlerFactory} is responsible for instantiating 
     * service handlers dynamically based on the provided configuration and other parameters.
     * 
     * @return an instance of {@link IServiceHandlerFactory} for creating service handlers.
     */
    IServiceHandlerFactory getServiceHandlerFactory();
}


