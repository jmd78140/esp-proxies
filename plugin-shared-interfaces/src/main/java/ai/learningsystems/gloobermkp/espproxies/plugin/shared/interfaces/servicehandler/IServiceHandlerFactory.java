package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.proxy.IProxyService;

/**
 * Factory interface for creating instances of {@link IServiceHandler}.
 * <p>
 * This interface defines a method for instantiating service handlers, 
 * allowing plugins or other components to provide their own implementations 
 * of {@link IServiceHandler}, initialized with the required dependencies.
 */
public interface IServiceHandlerFactory {

    /**
     * Creates a new instance of {@link IServiceHandler} using the provided proxy service.
     * 
     * @param proxy an instance of {@link IProxyService} that the created service handler will use 
     *              to execute requests and manage service-related operations.
     * @return a new instance of {@link IServiceHandler}.
     */
    IServiceHandler createServiceHandler(final IProxyService proxy);
}