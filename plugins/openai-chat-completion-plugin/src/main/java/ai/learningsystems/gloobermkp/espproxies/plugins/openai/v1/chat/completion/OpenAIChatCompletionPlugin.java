package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion;

import java.nio.file.Path;

import org.pf4j.Extension;
import org.pf4j.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IPlugin;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandlerFactory;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.exception.ServiceHandlerConfigFileLoadException;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.exception.ServiceHandlerConfigFileParseException;
import ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.configurationprovider.ServiceHandlerConfigProvider;


public class OpenAIChatCompletionPlugin extends Plugin
{
    private static Logger log = LoggerFactory.getLogger(OpenAIChatCompletionPlugin.class);
    
    public OpenAIChatCompletionPlugin() {
        
    }

    
    @Override
    public void start(){  
        
        log.info("OpenAIChatCompletionPlugin Started");
        
    }

    
    @Override
    public void stop(){
        
        log.info("OpenAIChatCompletionPlugin Stopped");
    }
    
    @Extension
    public static class OpenAIChatCompletionConfigurationProvider implements IPlugin {

        
        final private static String CONFIG_FILE_PATH = "service-config.yml";
        
        
        @Override
        public ServiceHandlerConfiguration getServiceHandlerConfiguration()
        throws ServiceHandlerConfigFileParseException, ServiceHandlerConfigFileLoadException
        {
            ClassLoader pluginClassLoader = this.getClass().getClassLoader();

            ServiceHandlerConfigProvider serviceHandlerConfigProvider = new ServiceHandlerConfigProvider(
                    Path.of(CONFIG_FILE_PATH),
                    pluginClassLoader
                );
            return serviceHandlerConfigProvider.load();
        }

        
        @Override
        public IServiceHandlerFactory getServiceHandlerFactory()
        {
            return new ServiceHandlerFactory();
        }
        
    }
    
}
