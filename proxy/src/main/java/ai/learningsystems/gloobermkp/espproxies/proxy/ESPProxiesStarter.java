package ai.learningsystems.gloobermkp.espproxies.proxy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "ai.learningsystems.gloobermkp.espproxies.proxy")
public class ESPProxiesStarter
{
    
    public static void main(String[] args) {
    
        SpringApplication.run(ESPProxiesStarter.class, args);
    }
    
}
