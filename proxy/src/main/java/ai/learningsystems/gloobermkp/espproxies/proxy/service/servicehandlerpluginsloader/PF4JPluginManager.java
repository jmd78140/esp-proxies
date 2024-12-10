package ai.learningsystems.gloobermkp.espproxies.proxy.service.servicehandlerpluginsloader;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.pf4j.PluginManager;
import org.pf4j.PluginWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IPlugin;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandlerFactory;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.IServiceHandlerRegistry;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.exception.ServiceHandlerConfigFileLoadException;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.exception.ServiceHandlerConfigFileParseException;
import ai.learningsystems.gloobermkp.espproxies.proxy.service.configuration.ProxyCoreConfigurationProvider;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class PF4JPluginManager
{
    private final IServiceHandlerRegistry serviceHandlerRegistry;
    
    private final Path pluginsDir;
    private final PluginManager pluginManager;

    @Autowired
    public PF4JPluginManager(final ProxyCoreConfigurationProvider configProvider, 
            final IServiceHandlerRegistry serviceHandlerRegistry) {
        
        this.serviceHandlerRegistry = serviceHandlerRegistry;
        this.pluginsDir =    Paths.get(configProvider.getPluginsFolder());
        this.pluginManager = new CustomPluginManager(pluginsDir); 
    }
        
    
    public void loadPluginsAndRegisterServiceHandlers() {
        
        pluginManager.loadPlugins();
        pluginManager.startPlugins();

        log.info("Plugins loaded:");
        for (final PluginWrapper plugin : pluginManager.getPlugins()) {
            log.info("Plugin: {}, Version: {}", plugin.getPluginId(), plugin.getDescriptor().getVersion());
        }
        
        registerPluginServiceHandlers();
    }

    
    
    public PluginManager getPluginManager() {
        return pluginManager;
    }
    
    
    public void reloadPlugins() {
    
        pluginManager.stopPlugins();
        pluginManager.unloadPlugins();
        
        loadPluginsAndRegisterServiceHandlers();
    }
    
    
    private void registerPluginServiceHandlers() {
        
        List<IPlugin> plugins = pluginManager.getExtensions(IPlugin.class);

        for (IPlugin plugin : plugins) {
            try {
             
                String pluginId = pluginManager.whichPlugin(plugin.getClass()).getPluginId();
                String pluginVersion = pluginManager.whichPlugin(plugin.getClass()).getDescriptor().getVersion();
                ServiceHandlerConfiguration configuration = plugin.getServiceHandlerConfiguration();
                IServiceHandlerFactory factory = plugin.getServiceHandlerFactory();

                serviceHandlerRegistry.registerServiceHandler(pluginId, pluginVersion, configuration, factory);
                log.info("Register ServiceHandler for plugin ID: {}, Version: {}", pluginId, pluginVersion);

            } 
            catch (ServiceHandlerConfigFileParseException | ServiceHandlerConfigFileLoadException pluginLoadConfigException) {
                log.error("Errors occured while loading plugin configuration : {}", plugin.getClass().getName(), pluginLoadConfigException);
            } 
            catch (Exception pluginRegistrationException) {
                log.error("Unattended errors have occured while registering plugin : {}", plugin.getClass().getName(), pluginRegistrationException);
            }
        }
    }
    
   
}


