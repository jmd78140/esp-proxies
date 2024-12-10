package ai.learningsystems.gloobermkp.espproxies.proxy.service.servicehandlerregistry;


import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandlerFactory;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.IServiceHandlerMetaData;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.IServiceHandlerRegistry;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration.ServiceProperties;


@Service
public class ServiceHandlerRegistry implements IServiceHandlerRegistry
{

    private final Map<String, IServiceHandlerMetaData> serviceHandlers = new ConcurrentHashMap<>();


    @Override
    public void registerServiceHandler(final String pluginId, final String pluginVersion,
            final ServiceHandlerConfiguration serviceHandlerConfiguration,
            final IServiceHandlerFactory serviceHandlerFactory)
    {

        Assert.notNull(pluginId,
                "registerServiceHandler(pluginId, pluginVersion, serviceHandlerConfiguration, serviceHandlerFactory)"
                        + " called with null pluginId !");
        Assert.hasText(pluginId,
                "registerServiceHandler(pluginId, pluginVersion, serviceHandlerConfiguration, serviceHandlerFactory)"
                        + " called with empty or blank pluginId !");
        Assert.notNull(pluginVersion,
                "registerServiceHandler(pluginId, pluginVersion, serviceHandlerConfiguration, serviceHandlerFactory)"
                        + " called with null pluginVersionpluginVersion !");
        Assert.hasText(pluginVersion,
                "registerServiceHandler(pluginId, pluginVersion, serviceHandlerConfiguration, serviceHandlerFactory)"
                        + " called with empty or blank pluginVersion !");
        Assert.notNull(serviceHandlerConfiguration,
                "registerServiceHandler(pluginId, pluginVersion, serviceHandlerConfiguration, serviceHandlerFactory)"
                        + " called with null serviceHandlerConfiguration !");
        Assert.notNull(serviceHandlerFactory,
                "registerServiceHandler(pluginId, pluginVersion, serviceHandlerConfiguration, serviceHandlerFactory)"
                        + " called with null serviceHandlerFactory !");

        final ServiceProperties serviceProperties = serviceHandlerConfiguration.getServiceProperties();

        final String serviceName = serviceProperties.getServiceName();
        if (null == serviceName || serviceName.isBlank())
            throw new TargetServiceNameException(
                    "Service name is either null or blank check ServiceHandler configuration ServiceName !");
        final String                  targetServiceBaseURL    = serviceProperties.getTargetServiceBaseUrl();
        final String                  targetServiceEndPoint   = serviceProperties.getTargetServiceEndPoint();
        final TargetServiceURIBuilder targetServiceURIBuilder = new TargetServiceURIBuilder(targetServiceBaseURL,
                targetServiceEndPoint);
        final Optional<URI>           optTargetServiceURI     = targetServiceURIBuilder.build();
        if (optTargetServiceURI.isEmpty())
            throw new TargetServiceURIException(
                    "TargetService URI is invalid check ServiceHandler configuration targetServiceBaseURL and targetServiceEndPoint!");
        final URI targetServiceURI = optTargetServiceURI.get();

        final ServiceHandlerMetaData serviceHandlerMetaData = new ServiceHandlerMetaData(pluginId, pluginVersion,
                targetServiceURI, serviceHandlerConfiguration, serviceHandlerFactory);

        this.serviceHandlers.put(serviceName, serviceHandlerMetaData);

    }


    @Override
    public String getPluginId(String serviceName)
    {

        final IServiceHandlerMetaData serviceHandlerMetaData = getServiceHandlerMetaData(serviceName);
        return serviceHandlerMetaData.getPluginId();

    }


    @Override
    public String getPluginVersion(String serviceName)
    {

        final IServiceHandlerMetaData serviceHandlerMetaData = getServiceHandlerMetaData(serviceName);
        return serviceHandlerMetaData.getPluginVersion();

    }


    @Override
    public IServiceHandlerFactory getServiceHandlerFactory(String serviceName)
    {

        final IServiceHandlerMetaData serviceHandlerMetaData = getServiceHandlerMetaData(serviceName);
        return serviceHandlerMetaData.getServiceHandlerFactory();
    }


    @Override
    public ServiceHandlerConfiguration getServiceHandlerConfiguration(String serviceName)
    {

        final IServiceHandlerMetaData serviceHandlerMetaData = getServiceHandlerMetaData(serviceName);
        return serviceHandlerMetaData.getServiceHandlerConfiguration();
    }


    @Override
    public URI getTargetServiceURI(String serviceName)
    {

        final IServiceHandlerMetaData serviceHandlerMetaData = getServiceHandlerMetaData(serviceName);
        return serviceHandlerMetaData.getTargetServiceURI();
    }


    @Override
    public IServiceHandlerMetaData getServiceHandlerMetaData(String serviceName)
    {

        final IServiceHandlerMetaData serviceHandlerMetaData = this.serviceHandlers.get(serviceName);
        if (null == serviceHandlerMetaData)
            throw new UnknownServiceException("Serice name : " + serviceName + " not found !");
        return serviceHandlerMetaData;
    }


    @Override
    public Map<String, IServiceHandlerMetaData> getAllServiceHandlers()
    {

        return this.serviceHandlers;
    }


}
