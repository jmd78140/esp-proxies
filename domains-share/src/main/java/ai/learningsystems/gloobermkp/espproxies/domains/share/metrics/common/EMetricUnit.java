package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common;

public enum EMetricUnit {
    
    REQ_BY_SEC("Request/seconde", "Req./s", "# Request by second", "number of requests by second"),
    ESP_SERVICE_RESPONSE_TIME("ESP Response Time (ms)", "ESP SRT (ms)", "ESP Service Response Time in ms", "External Service Provider Service Response Time (request end - request start) ms"),
    TOKEN("token", "tok.", "# tokens", "number of tokens");
    
    final private String label;
    final private MetricUnitDetails details;
    
    private EMetricUnit(final String label, 
            final String shortForm, 
            final String longForm, 
            final String description) {
        
        this.label      = label;
        this.details    = new MetricUnitDetails(shortForm, longForm, description);
    }

    
    public String getLabel()
    {
    
        return label;
    }

    
    public MetricUnitDetails getDetails()
    {
    
        return details;
    }
        
}
