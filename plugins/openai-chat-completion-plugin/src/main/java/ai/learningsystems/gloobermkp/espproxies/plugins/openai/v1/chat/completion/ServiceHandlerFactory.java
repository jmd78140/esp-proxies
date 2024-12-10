package ai.learningsystems.gloobermkp.espproxies.plugins.openai.v1.chat.completion;

import org.springframework.util.Assert;

import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.proxy.IProxyService;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandler;
import ai.learningsystems.gloobermkp.espproxies.plugin.shared.interfaces.servicehandler.IServiceHandlerFactory;

public class ServiceHandlerFactory implements IServiceHandlerFactory
{

    @Override
    public IServiceHandler createServiceHandler(IProxyService proxy)
    {
       Assert.notNull(proxy, "createServiceHandler(proxy) called with null proxy !");
       
       return new OpenAIChatCompletionServiceHandler(proxy);
    }

}
