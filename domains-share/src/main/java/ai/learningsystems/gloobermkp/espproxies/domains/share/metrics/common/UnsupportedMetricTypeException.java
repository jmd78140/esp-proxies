package ai.learningsystems.gloobermkp.espproxies.domains.share.metrics.common;


public class UnsupportedMetricTypeException extends RuntimeException 
{
    
    private static final long serialVersionUID = 3198013394596737513L;

    public UnsupportedMetricTypeException(final String message) {
        
        super("Unsupported metric type" +  message +"! Choose between : INT, FLOAT or STRING;");
    }

}
