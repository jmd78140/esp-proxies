package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common;

import org.springframework.util.Assert;

public enum EMetricType {
    
    INT("INT"), 
    DOUBLE("DOUBLE"), 
    STRING("STRING");
    
    private String type;
    
    private EMetricType(final String type) {
        this.type = type;
    }
    
    
    public static EMetricType parse(String type) {
        
        Assert.hasText(type, "parse(type) called with null or empty type !");
        
        for (EMetricType metricType : EMetricType.values()) {
            if (metricType.getType().equals(type)) {
                return metricType;
            }
        }
        throw new UnsupportedMetricTypeException(type);
    }
 
    
    public String getType() {
        return type;
    }
}
