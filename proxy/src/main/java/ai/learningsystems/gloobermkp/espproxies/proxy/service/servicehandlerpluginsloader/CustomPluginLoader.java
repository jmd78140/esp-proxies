package ai.learningsystems.gloobermkp.espproxies.proxy.service.servicehandlerpluginsloader;

import java.nio.file.Path;

import org.pf4j.ClassLoadingStrategy;
import org.pf4j.DefaultPluginLoader;
import org.pf4j.PluginClassLoader;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginManager;

/**
 * Allow to change class loader strategy to Parent A (Spring Boot App)
 * first before Plugin and Deoendencies i.e. ClassLoadingStrategy.APD.
 * Otherwise classes definitions loaded by Sping application class loader and
 * those loaded by the Plugin class loader are not aligned raizing : 
 * java.lang.LinkageError: loader constraint violation: 
 * loader org.pf4j.PluginClassLoader @59498d94 wants to load class org.springframework.XXXX.
 * A different class with the same name was previously loaded by 'app'. 
 * (org.springframework.XXXX is in unnamed module of loader 'app')
 */
public class CustomPluginLoader extends DefaultPluginLoader {

    public CustomPluginLoader(PluginManager pluginManager) {
        super(pluginManager);
    }

    @Override
    protected PluginClassLoader createPluginClassLoader(Path pluginPath, PluginDescriptor pluginDescriptor) {
        
        PluginClassLoader classLoader = new PluginClassLoader(pluginManager, pluginDescriptor, getClass().getClassLoader(), ClassLoadingStrategy.APD);
        return classLoader;
    }
}