package ai.learningsystems.gloobermkp.espproxies.proxy.service.servicehandlerregistry;

import java.net.URI;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandlerFactory;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.IServiceHandlerMetaData;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration;

public class ServiceHandlerMetaData implements IServiceHandlerMetaData 
{
    private final String pluginId;
    private final String pluginVersion;
    private  final URI targetServiceURI;
    private final ServiceHandlerConfiguration serviceHandlerConfiguration;
    private final IServiceHandlerFactory serviceHandlerFactory;
    
    
    public ServiceHandlerMetaData(final String pluginId, 
            final String pluginVersion,  
            final URI targetServiceURI,
            ServiceHandlerConfiguration serviceHandlerConfiguration, 
            IServiceHandlerFactory serviceHandlerFactory) 
    {

        super();
        this.pluginId                    = pluginId;
        this.pluginVersion               = pluginVersion;    
        this.targetServiceURI            = targetServiceURI;
        this.serviceHandlerConfiguration = serviceHandlerConfiguration;
        this.serviceHandlerFactory       = serviceHandlerFactory;
    }


    @Override
    public String getPluginId()
    {
    
        return pluginId;
    }


    @Override
    public String getPluginVersion()
    {
    
        return pluginVersion;
    }



    @Override
    public String getServiceName()
    {

        return this.serviceHandlerConfiguration.getServiceProperties().getServiceName();
    }


    @Override
    public URI getTargetServiceURI()
    {
    
        return targetServiceURI;
    }


    @Override
    public ServiceHandlerConfiguration getServiceHandlerConfiguration()
    {
    
        return serviceHandlerConfiguration;
    }


    @Override
    public IServiceHandlerFactory getServiceHandlerFactory()
    {
    
        return serviceHandlerFactory;
    }


    @Override
    public String toJson()
    {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(this);
        } 
        catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting PluginMetadata to JSON", e);
        }
    }
    
}


