package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.usagemetricssupplier;

import static ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.OpenAIChatCompletionConstants.SSE_ENDOFSTREAM_MARKER;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.ModelType;

public class StreamResponseTokenCounter
{
    
    private static Logger log = LoggerFactory.getLogger(StreamResponseTokenCounter.class);
    
    private final List<String> completeResponse;
    
    public StreamResponseTokenCounter(final List<String> completeResponse) {
        
        this.completeResponse = completeResponse;
    }
    
    
    /**
     * Count token in reply for SSE exchange.
     */
    public int count()
    {
        String consolidatedSSEContent;
        try {
            consolidatedSSEContent = extractAndConcatResponseContents();
            int tokenCount = Encodings.newDefaultEncodingRegistry()
                    .getEncodingForModel(ModelType.GPT_4O)
                    .countTokens(consolidatedSSEContent);
            log.info("countTokensInResponse() request : \n {} \n count: {}", consolidatedSSEContent, tokenCount);
            return tokenCount;
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return -1;
    }

    
    private String extractAndConcatResponseContents() throws IOException
    {

        final List<String> extractMessages = extractResponseContents();
        return concatExtractedResponseContents(extractMessages);
    }


    private List<String> extractResponseContents() throws IOException
    {
            
        final ObjectMapper objectMapper      = new ObjectMapper();
        final List<String> extractedContents = new ArrayList<>();
        for (String jsonResponse : completeResponse) {

            if (SSE_ENDOFSTREAM_MARKER.equals(jsonResponse.trim())) {
                continue;
            }
            try {
               
                final JsonNode rootNode    = objectMapper.readTree(jsonResponse);
                final JsonNode choicesNode = rootNode.path("choices");
                for (final JsonNode choiceNode : choicesNode) {
                    final String content = choiceNode.path("delta").path("content").asText();
                    if (!content.isEmpty()) {
                        extractedContents.add(content);
                    }
                }
            }
            catch (IOException ioEx) {
                log.error("extractResponseContents() parsing error on : \n{}\n continuing to next response ! ", jsonResponse);
                continue;
            }
        }
        return extractedContents;
    }


    private String concatExtractedResponseContents(final List<String> extractedContents)
    {

        return String.join(" ", extractedContents);
    }
    
}
