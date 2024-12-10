package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.usage;

import ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common.EMetricUnit;

public class TokenCountUsageMetric extends UsageMetric {

    private final static String TOKEN_COUNT = "TokenCount";
    
    public TokenCountUsageMetric(int value) {

        super(TOKEN_COUNT, value, EMetricUnit.TOKEN);
    }

}
    

