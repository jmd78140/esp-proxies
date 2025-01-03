package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.endofstreamdetector;

import static ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.OpenAIChatCompletionConstants.SSE_ENDOFSTREAM_MARKER;

public class EndOfStreamDetector
{
    
    public static boolean isEndOfStream(final String chunk)
    {
        final boolean isEndOfStream = chunk.contains(SSE_ENDOFSTREAM_MARKER);
        return isEndOfStream;
    }
    
    public static String getEndOfStreamMarker() {
        
        return SSE_ENDOFSTREAM_MARKER;
    }
}
