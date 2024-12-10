package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.technical;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common.EMetricUnit;
import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common.Metric;


@JsonSerialize(using = TechnicalMetricSerializer.class)
public class TechnicalMetric extends Metric
{

    public TechnicalMetric(final String name, final int value, final EMetricUnit unit ) {

        super(name, value, unit);
    }
    
    
    public TechnicalMetric(final String name, final double value, final EMetricUnit unit) {

        super(name, value, unit);
    }

    
    public TechnicalMetric(final String name, final String value, final EMetricUnit unit) {

        super(name, value, unit);
    }
}
