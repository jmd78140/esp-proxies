package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Metrics <T extends Metric>
{
    private Map<String, T> metrics;
    
    
    public Metrics() {
        
        this.metrics = new HashMap<>();
    }
    
    
    public void addMetric(final String metricName, final T metric) {
        
        this.metrics.put(metricName, metric);
    }

    
    public String toJson()
    {

        try {
            return new ObjectMapper().writeValueAsString(metrics);
        }
        catch (Exception e) {
            throw new RuntimeException("Error converting metrics to JSON", e);
        }
    }
}
