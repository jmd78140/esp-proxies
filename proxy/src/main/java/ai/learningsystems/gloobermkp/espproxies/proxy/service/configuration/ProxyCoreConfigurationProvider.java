package ai.learningsystems.gloobermkp.espproxies.proxy.service.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProxyCoreConfigurationProvider
{
    
    // proxy.plugins.folderpath: : proxy plugins folder 
    // default to PROXY_HOME/plugins : if not defined
    @Value("${proxy.plugins.folderpath:./plugins}")
    private String PLUGINS_FOLDER_PATH;
    public String getPluginsFolder() { return PLUGINS_FOLDER_PATH;}

}
