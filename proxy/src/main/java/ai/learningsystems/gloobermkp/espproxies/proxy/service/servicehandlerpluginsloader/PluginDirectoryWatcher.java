package ai.learningsystems.gloobermkp.espproxies.proxy.service.servicehandlerpluginsloader;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ai.learningsystems.gloobermkp.espproxies.proxy.service.configuration.ProxyCoreConfigurationProvider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class PluginDirectoryWatcher implements DisposableBean {

    private final ProxyCoreConfigurationProvider configProvider;
    private final PF4JPluginManager pf4jPluginManager;
    private final Executor executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private WatchService watchService;
    private Thread watchThread;

    @Autowired
    public PluginDirectoryWatcher( final ProxyCoreConfigurationProvider configProvider,
            final PF4JPluginManager pf4jPluginManager, 
            final Executor executor) {
        
        this.configProvider = configProvider;
        this.pf4jPluginManager = pf4jPluginManager;
        this.executor = executor;
    }

    /**
     * Initialisation : starts watching plugins folder.
     */
    @PostConstruct
    public void startWatching() throws Exception {
        
        Path pluginsDirectory = Paths.get(configProvider.getPluginsFolder());
        log.info("Monitoring plugin directory: {}", pluginsDirectory);

        pf4jPluginManager.loadPluginsAndRegisterServiceHandlers();

        watchThread = new Thread(() -> {
            try {
                startDirectoryWatch(pluginsDirectory);
            } catch (Exception e) {
                log.error("Error while watching plugin directory", e);
            }
        });

        executor.execute(watchThread);
        running.set(true);
    }

    /**
     * Monitor modifications in plugins folder.
     */
    private void startDirectoryWatch(Path pluginsDirectory) throws Exception {
       
        watchService = FileSystems.getDefault().newWatchService();
        pluginsDirectory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);
        log.info("Watching plugin directory: {}", pluginsDirectory);

        while (running.get()) {
            WatchKey key = watchService.take(); // Bloque jusqu'à un événement
            for (WatchEvent<?> event : key.pollEvents()) {
                String fileName = event.context().toString();
                if (fileName.endsWith(".jar") || fileName.endsWith(".zip")) {
                    log.info("Plugin file change detected: {}", fileName);
                    reloadPlugins();
                }
            }
            key.reset();
        }
    }

    
    /**
     * Reload Plugins on changes detection
     */
    private void reloadPlugins() {
       
        try {
            log.info("Reloading plugins...");
            pf4jPluginManager.reloadPlugins();
            log.info("Plugins reloaded successfully.");
        } catch (Exception e) {
            log.error("Error while reloading plugins", e);
        }
    }

    
    /**
     * Component shutdown cleaning 
     */
    @PreDestroy
    @Override
    public void destroy() throws Exception {
        
        log.info("Shutting down plugin directory watcher");

        // Stop Monitoring 
        running.set(false);
        if (watchService != null) {
            watchService.close();
        }

        if (watchThread != null && watchThread.isAlive()) {
            watchThread.interrupt();
        }
    }
}