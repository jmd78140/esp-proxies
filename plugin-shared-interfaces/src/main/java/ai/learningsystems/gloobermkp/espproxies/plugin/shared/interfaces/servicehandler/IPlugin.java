package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler;

import org.pf4j.ExtensionPoint;

/**
 * Represents a plugin interface that combines multiple responsibilities and acts as an extension point in the PF4J framework.
 * <p>
 * This interface extends the following:
 * <ul>
 *   <li>{@link IServiceHandlerFactory} - Provides a factory method for creating instances of {@link IServiceHandler}.</li>
 *   <li>{@link IServiceHandler} - Handles HTTP requests, supplies usage metrics, and detects end-of-stream markers in streaming responses.</li>
 *   <li>{@link ExtensionPoint} - Marks this interface as an extension point in the PF4J plugin framework.</li>
 * </ul>
 * By implementing this interface, a plugin can act as a complete service handler while integrating seamlessly with 
 * the PF4J framework for dynamic plugin management.
 */
public interface IPlugin extends IServiceHandlerConfigurationProvider, ExtensionPoint {

}