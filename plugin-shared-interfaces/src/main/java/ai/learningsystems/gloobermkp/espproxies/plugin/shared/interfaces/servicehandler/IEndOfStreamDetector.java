package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler;


/**
 * Interface for detecting the end of a Server-Sent Events (SSE) stream.
 * <p>
 * Implementing this interface allows a plugin to define a mechanism for recognizing
 * the end-of-stream marker in SSE streams, as this marker can vary depending on the
 * provider's implementation.
 */
public interface IEndOfStreamDetector {

    /**
     * Determines whether the given chunk represents the end-of-stream marker in an SSE stream.
     * 
     * @param chunk a {@link String} representing a single chunk received in the SSE stream.
     * @return {@code true} if the chunk contains the end-of-stream marker, {@code false} otherwise.
     */
    boolean isEndOfStream(String chunk);
}