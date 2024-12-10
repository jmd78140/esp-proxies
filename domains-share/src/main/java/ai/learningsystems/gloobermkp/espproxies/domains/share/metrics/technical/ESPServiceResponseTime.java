package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.technical;

import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common.EMetricUnit;

public class ESPServiceResponseTime extends TechnicalMetric
{
    private final static String ESP_SERVICE_RESPONSE_TIME = "ESP_SRT";
    
    private final long requestStartTimeMs;
    private final long requestEndTimeMs;  
    
    
    public ESPServiceResponseTime(final long requestStartTimeMs, final long requestEndTimeMs) {
        
        super(ESP_SERVICE_RESPONSE_TIME, requestEndTimeMs - requestStartTimeMs, EMetricUnit.ESP_SERVICE_RESPONSE_TIME);
        this.requestStartTimeMs = requestStartTimeMs;
        this.requestEndTimeMs = requestEndTimeMs;
    }


    public long getRequestStartTimeMs()
    {
    
        return requestStartTimeMs;
    }

    
    public long getRequestEndTimeMs()
    {
    
        return requestEndTimeMs;
    }

    
    public long getRequestResponseTimeMs()
    {
        return requestEndTimeMs - requestStartTimeMs;
    }
    
    
}
