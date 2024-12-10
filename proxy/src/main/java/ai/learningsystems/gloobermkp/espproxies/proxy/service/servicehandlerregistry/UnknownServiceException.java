package ai.learningsystems.gloobermkp.espproxies.proxy.service.servicehandlerregistry;


public class UnknownServiceException extends RuntimeException 
{
    
    private static final long serialVersionUID = 3012382068193407548L;

    public UnknownServiceException(final String message) {    
        super(message);
    }

}
