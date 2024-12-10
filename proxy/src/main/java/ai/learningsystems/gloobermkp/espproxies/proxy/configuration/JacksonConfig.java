package ai.learningsystems.gloobermkp.espproxies.proxy.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ai.learningsystems.gloobermkp.espproxies.proxy.controller.serializer.ServerSentEventSerializer;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        // Create a module to register the custom serializer for ServerSentEvent<String>
        SimpleModule module = new SimpleModule();
        
        // Register the serializer for ServerSentEvent<String>
        module.addSerializer(new ServerSentEventSerializer());

        // Register the module
        objectMapper.registerModule(module);

        return objectMapper;
    }
}