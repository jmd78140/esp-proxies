package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry;

import java.net.URI;
import java.util.Map;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandlerFactory;

/**
 * Interface for managing the registration and retrieval of service handlers and their associated metadata.
 * <p>
 * This registry provides methods to register service handlers along with their configurations and factories,
 * and to retrieve information about registered handlers based on the service name.
 * It acts as a central repository for managing service-related metadata in a plugin-based architecture.
 */
public interface IServiceHandlerRegistry
{
    /**
     * Registers a service handler configuration and factory.
     * @param pluginId a {@link String} representing the pluginId from which ServiceHandler is loaded.
     * @param pluginId a {@link String} representing the pluginVersion from which ServiceHandler is loaded.
     * @param serviceHandlerConfiguration the {@link ServiceHandlerConfiguration} associated with the service handler.
     * @param serviceHandlerFactory the {@link IServiceHandlerFactory} used to create instances of the service handler.
     */
    public void registerServiceHandler(
            final String pluginId,
            final String pluginVersion,
            final ServiceHandlerConfiguration serviceHandlerConfiguration,
            final IServiceHandlerFactory serviceHandlerFactory);
    /**
     * Retrieves the plugin ID associated with a given service name.
     * 
     * @param serviceName the name of the service whose plugin ID is to be retrieved.
     * @return the plugin ID as a {@link String}, or {@code null} if the service is not registered.
     */
    public String getPluginId(final String serviceName);
   
    /**
     * Retrieves the plugin version associated with a given service name.
     * 
     * @param serviceName the name of the service whose plugin version is to be retrieved.
     * @return the plugin version as a {@link String}, or {@code null} if the service is not registered.
     */
    public String getPluginVersion(final String serviceName);
   
    
    /**
     * Retrieves the service handler factory associated with a given service name.
     * 
     * @param serviceName the name of the service whose handler factory is to be retrieved.
     * @return an instance of {@link IServiceHandlerFactory}, or {@code null} if the service is not registered.
     */
    public IServiceHandlerFactory getServiceHandlerFactory(final String serviceName);
   
    
    /**
     * Retrieves the service handler configuration associated with a given service name.
     * 
     * @param serviceName the name of the service whose configuration is to be retrieved.
     * @return the {@link ServiceHandlerConfiguration} for the service, or {@code null} if the service is not registered.
     */
    public ServiceHandlerConfiguration getServiceHandlerConfiguration(final String serviceName);
    
    
    /**
     * Retrieves the target service URI for a given service name.
     * 
     * @param serviceName the name of the service whose target URI is to be retrieved.
     * @return the {@link URI} of the target service, or {@code null} if the service is not registered.
     */
    public URI getTargetServiceURI(final String serviceName);
    
    
    /**
     * Retrieves the metadata associated with a given service name.
     * 
     * @param serviceName the name of the service whose metadata is to be retrieved.
     * @return an instance of {@link IServiceHandlerMetaData}, or {@code null} if the service is not registered.
     */
    public IServiceHandlerMetaData getServiceHandlerMetaData(final String serviceName);
    
    
    /**
     * Retrieves metadata for all registered service handlers.
     * 
     * @return a {@link Map} where the keys are service names and the values are instances of {@link IServiceHandlerMetaData}.
     */
    public Map<String, IServiceHandlerMetaData> getAllServiceHandlers();
}
