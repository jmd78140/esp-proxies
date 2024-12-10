package ai.learningsystems.gloobermkp.espproxies.proxy.service.servicehandlerpluginsloader;

import java.nio.file.Path;

import org.pf4j.DefaultPluginManager;
import org.pf4j.PluginLoader;


public class CustomPluginManager extends DefaultPluginManager {

    public CustomPluginManager(Path pluginsRoot) {
        super(pluginsRoot);
    }

    @Override
    protected PluginLoader createPluginLoader() {
        
        return new CustomPluginLoader(this);
    }
}
