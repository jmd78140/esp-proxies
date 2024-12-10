package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common.EMetricUnit;
import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common.Metric;


@JsonSerialize(using = UsageMetricSerializer.class)
public class UsageMetric extends Metric
{
    
    public UsageMetric(final String name, final int value, final EMetricUnit unit ) {

        super(name, value, unit);
    }
    
    
    public UsageMetric(final String name, final double value, final EMetricUnit unit) {

        super(name, value, unit);
    }

    
    public UsageMetric(final String name, final String value, final EMetricUnit unit) {

        super(name, value, unit);
    }
        
}
