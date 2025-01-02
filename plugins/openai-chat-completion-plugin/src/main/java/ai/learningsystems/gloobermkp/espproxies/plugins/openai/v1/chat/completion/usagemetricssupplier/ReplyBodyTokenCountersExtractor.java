package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.usagemetricssupplier;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * Extract the token counters from the standard reply body :
 * { ...,
 *    "usage": {
 *        "prompt_tokens": 13,
 *        "completion_tokens": 26,
 *        "total_tokens": 39,
 *        ...
 *    },
 *   ...
 * }
 */
public class ReplyBodyTokenCountersExtractor
{

    private final String       responseBody;
    private final ObjectMapper objectMapper;


    public ReplyBodyTokenCountersExtractor(String responseBody) {

        this.responseBody = responseBody;
        this.objectMapper = new ObjectMapper();
    }


    public OAITokenCounters extractTokenCounters()
    {

        try {
            JsonNode usageNode = parseUsageNode();
            return extractCountersFromUsageNode(usageNode);
        }
        catch (Exception exception) {
            throw new RuntimeException("Error extracting usage metrics from response", exception);
        }
    }


    private JsonNode parseUsageNode() throws Exception
    {

        JsonNode parsedResponseBody = objectMapper.readTree(responseBody);
        return parsedResponseBody.path("usage");
    }


    private OAITokenCounters extractCountersFromUsageNode(JsonNode usageNode)
    {

        int promptTokensCount     = usageNode.path("prompt_tokens").asInt();
        int completionTokensCount = usageNode.path("completion_tokens").asInt();
        int totalTokensCount      = usageNode.path("total_tokens").asInt();
        return new OAITokenCounters(promptTokensCount, completionTokensCount, totalTokensCount);
    }

}