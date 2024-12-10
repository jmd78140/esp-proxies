package ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry;

import java.net.URI;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandlerFactory;

public interface IServiceHandlerMetaData
{
    public String getPluginId();
    public String getPluginVersion();
    
    public String getServiceName();   
    public URI getTargetServiceURI();
    public ServiceHandlerConfiguration getServiceHandlerConfiguration();
    public IServiceHandlerFactory getServiceHandlerFactory();
    
    public String toJson();

}