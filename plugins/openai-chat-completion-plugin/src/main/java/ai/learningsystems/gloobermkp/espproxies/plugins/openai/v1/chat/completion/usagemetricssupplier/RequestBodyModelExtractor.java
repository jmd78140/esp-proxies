package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.usagemetricssupplier;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.learningsystems.gloobermkp.external.commons.domains.llm.ELLMEngineModel;



public class RequestBodyModelExtractor
{

    private final String requestBody;


    public RequestBodyModelExtractor(final String requestBody) {

        this.requestBody = requestBody;
    }


    public ELLMEngineModel getLLMModel()
    {

        return extractLLMModel();
    }


    private ELLMEngineModel extractLLMModel()
    {

        try {
            final ObjectMapper objectMapper      = new ObjectMapper();
            final JsonNode     parsedRequestBody = objectMapper.readTree(requestBody);
            final JsonNode     modelNode         = parsedRequestBody.path("model");
            final String       model             = modelNode.asText();
            return ELLMEngineModel.valueOf(model);
        }
        catch (Exception jsonParseException) {
            throw new RuntimeException("Error extracting llm model from request", jsonParseException);
        }
    }


}
