package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;


public class UsageMetricSerializer extends JsonSerializer<UsageMetric>
{
    @Override
    public void serialize(UsageMetric metric, JsonGenerator gen, SerializerProvider serializers) throws IOException {
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
