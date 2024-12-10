package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.knuddels.jtokkit.Encodings;
import com.knuddels.jtokkit.api.ModelType;


public class StreamRequestTokenCounter
{
    private static Logger log = LoggerFactory.getLogger(StreamRequestTokenCounter.class);
   
    
    private final String request;
    
    
    public StreamRequestTokenCounter(final String request) {
        
        this.request = request;
    }
    
    /**
     * Count token in request for SSE exchange.
     */
    public int count()
    {

        try {
            final String promptPart = extractAndConcatRequestMessages();
            final int    tokenCount = Encodings.newDefaultEncodingRegistry().getEncodingForModel(ModelType.GPT_4O)
                    .countTokens(promptPart);
            log.info("countTokensInRequest() request : \n {} \n count: {}", promptPart, tokenCount);
            return tokenCount;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    
    private String extractAndConcatRequestMessages() throws IOException
    {

        final List<String> extractMessages = extractRequestMessages();
        return concatRequestMessageContents(extractMessages);
    }
    
    
    private List<String> extractRequestMessages() throws IOException
    {

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode     rootNode     = objectMapper.readTree(request);
        JsonNode     messagesNode = rootNode.path("messages");

        List<String>       messageContents  = new ArrayList<>();
        Iterator<JsonNode> messagesIterator = messagesNode.iterator();

        while (messagesIterator.hasNext()) {
            JsonNode message = messagesIterator.next();
            String   content = message.path("content").asText();
            messageContents.add(content);
        }
        return messageContents;
    }


    private String concatRequestMessageContents(final List<String> messageContents)
    {

        return String.join(" ", messageContents);
    }

}
