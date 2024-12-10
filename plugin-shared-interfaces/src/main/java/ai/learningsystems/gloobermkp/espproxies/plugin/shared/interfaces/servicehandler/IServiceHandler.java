package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler;


/**
 * A composite interface that combines multiple responsibilities for handling service requests, 
 * providing usage metrics, and detecting end-of-stream markers in streaming responses.
 * <p>
 * This interface extends the following:
 * <ul>
 *   <li>{@link IRequestHandler} - For managing HTTP requests and responses.</li>
 *   <li>{@link IUsageMetricsSupplier} - For calculating and providing usage metrics for both standard and streaming responses.</li>
 *   <li>{@link IEndOfStreamDetector} - For detecting end-of-stream markers in Server-Sent Events (SSE) or other streaming protocols.</li>
 * </ul>
 * By implementing this interface, a service handler is capable of processing requests, tracking usage metrics, 
 * and handling streaming-specific logic in a unified way.
 */
public interface IServiceHandler extends IRequestHandler, IUsageMetricsSupplier, IEndOfStreamDetector {

}