package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common;

import org.springframework.util.Assert;

public class Metric
{
    final String name;
    final EMetricType type;
    final String value;
    final EMetricUnit unit;
    
    
    public Metric(final String name, final int value, final EMetricUnit unit ) {

        this(name, EMetricType.INT, Integer.toString(value), unit);
    }
    
    
    public Metric(final String name, final double value, final EMetricUnit unit) {

        this(name, EMetricType.DOUBLE, Double.toString(value), unit);
    }
    

    public Metric(final String name, final String value, final EMetricUnit unit) {

        this(name, EMetricType.STRING, value, unit);
    }
    
    
    private Metric(final String name, 
            final EMetricType type, 
            final String value, 
            final EMetricUnit unit) {
        
        Assert.hasText(name, "Metric(name, type, value) called with null or empty name !");
        Assert.hasText(value, "Metric(name, type, value) called with null or empty value !");
        
        this.name = name;
        this.type = type;
        this.value = value;
        this.unit = unit;
    }


    public String getName()
    {
    
        return name;
    }

    
    public EMetricType getType()
    {
    
        return type;
    }

    
    public String getValue()
    {
    
        return value;
    }


    
    public EMetricUnit getUnit()
    {
    
        return unit;
    }
      

}
