package sfri.mhmd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import sfri.mhmd.utils.cdi.Context;
import sfri.mhmd.utils.cdi.ContextProvider;
import sfri.mhmd.utils.cdi.DependencyConstructor;
import sfri.mhmd.utils.cdi.DependencyInjector;
import sfri.mhmd.utils.server.LiteChatHandler;
import sfri.mhmd.utils.server.LiteChatHttpServer;
import sfri.mhmd.utils.server.ResourceHandler;
import sfri.mhmd.utils.server.ServerConfiguration;

public class Application {
    public static void main(String[] args) throws IOException {
        var di = new DependencyInjector(new ConcurrentHashMap<>());
        var dc = new DependencyConstructor(di);
        ContextProvider.setContextProvider(new ContextProvider(new ConcurrentHashMap<>()));
        ContextProvider.getContextProvider().addContext(new Context(Thread.currentThread().getName()), di);

        di.set(ServerConfiguration.class, new ServerConfiguration("/", new InetSocketAddress(8080)));
        di.setAll(LiteChatHandler.class, List.<LiteChatHandler>of(
                (x) -> {
                    var body = "Hello World".getBytes();
                    x.sendResponseHeaders(200, body.length);
                    x.getResponseBody().write(body);
                }, new ResourceHandler(".*")));

        startApp(di, dc);
    }

    private static void startApp(DependencyInjector di, DependencyConstructor dc) {
        var thread = new Thread(() -> {
            try {
                var serverInstance = dc.constructDeep(LiteChatHttpServer.class);
                di.set(LiteChatHttpServer.class, serverInstance);
                serverInstance.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            ContextProvider.getContextProvider().clearAllContexts();
            di.get(LiteChatHttpServer.class).stop();
        }
    }
}
