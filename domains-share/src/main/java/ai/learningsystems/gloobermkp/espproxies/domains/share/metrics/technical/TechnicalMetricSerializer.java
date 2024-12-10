package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.technical;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class TechnicalMetricSerializer extends JsonSerializer<TechnicalMetric>
{
   
        @Override
        public void serialize(TechnicalMetric metric, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject();
            
            // Serialize value
            gen.writeStringField("value", metric.getValue());
            
            // Serialize unit (label)
            gen.writeStringField("unit", metric.getUnit().getLabel());
            
            // Serialize type
            gen.writeStringField("type", metric.getType().name());
            
            gen.writeEndObject();
        }
    
}
