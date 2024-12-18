package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion.configurationprovider;

import java.io.InputStream;
import java.nio.file.Path;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.ServiceHandlerConfiguration;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.exception.ServiceHandlerConfigFileLoadException;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandlerregistry.exception.ServiceHandlerConfigFileParseException;



public class ServiceHandlerConfigProvider
{
    
    final Path configurationFilePath;
    final ClassLoader pluginClassLoader;

    public ServiceHandlerConfigProvider(final Path configurationFilePath, final ClassLoader pluginClassLoader) {
        this.configurationFilePath = configurationFilePath;
        this.pluginClassLoader = pluginClassLoader;
    }

    public ServiceHandlerConfiguration load()
            throws ServiceHandlerConfigFileParseException, ServiceHandlerConfigFileLoadException {

        try (InputStream inputStream = pluginClassLoader.getResourceAsStream(configurationFilePath.toString())) {
            if (inputStream == null) {
                throw new IllegalArgumentException("No such file or directory: " + configurationFilePath);
            }

            // Charger la configuration à partir de l'InputStream
            return ServiceHandlerConfiguration.parseYaml(inputStream);
        } catch (Exception ex) {
            throw new ServiceHandlerConfigFileLoadException("Failed to load configuration from: " + configurationFilePath + "\n" + ex);
        }
    }
    
}
