package ai.learningsystems.gloobermkp.espproxies.proxy.controller.serializer;


import java.io.IOException;

import org.springframework.http.codec.ServerSentEvent;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class ServerSentEventSerializer extends JsonSerializer<ServerSentEvent<String>>  {
    
    @Override
    public void serialize(ServerSentEvent<String> event, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("event", event.event());
        gen.writeStringField("id", event.id());
        gen.writeStringField("data", event.data());
        gen.writeEndObject();
    }
    
    @Override
    public Class<ServerSentEvent<String>> handledType() {
        return (Class<ServerSentEvent<String>>) (Class<?>) ServerSentEvent.class;
    }
}