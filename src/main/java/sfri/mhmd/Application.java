package sfri.mhmd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import sfri.mhmd.utils.cdi.Context;
import sfri.mhmd.utils.cdi.ContextProvider;
import sfri.mhmd.utils.cdi.dependency.DependencyConstructor;
import sfri.mhmd.utils.cdi.dependency.DependencyInjector;
import sfri.mhmd.utils.server.LiteChatController;
import sfri.mhmd.utils.server.LiteChatFrontController;
import sfri.mhmd.utils.server.LiteChatResourceResolver;
import sfri.mhmd.utils.server.ServerConfiguration;

public class Application {
    private static DependencyInjector di;
    private static DependencyConstructor dc;

    public static void main(String[] args) throws IOException {
        initContext();

        di.set(ServerConfiguration.class, new ServerConfiguration("/", new InetSocketAddress(8080)));
        di.setAll(LiteChatController.class, List.<LiteChatController>of(
                (x) -> {
                    var body = "Hello World".getBytes();
                    x.sendResponseHeaders(200, body.length);
                    x.getResponseBody().write(body);
                }, new LiteChatResourceResolver(".*")));

        startApp(di, dc);
    }

    private static void initContext() {
        di = new DependencyInjector(new ConcurrentHashMap<>());
        dc = new DependencyConstructor(di, new CopyOnWriteArrayList<>());
        ContextProvider.setContextProvider(new ContextProvider(new ConcurrentHashMap<>(), new ConcurrentHashMap<>()));
        ContextProvider.getContextProvider().addContext(new Context(Thread.currentThread().getName()), di, dc);
        ContextProvider.getContextProvider().setRootContextInjector(di);
        ContextProvider.getContextProvider().setRootContextConstructor(dc);
    }

    private static void startApp(DependencyInjector di, DependencyConstructor dc) {
        var thread = new Thread(() -> {
            try {
                var serverInstance = dc.constructDeep(LiteChatFrontController.class);
                di.set(LiteChatFrontController.class, serverInstance);
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
            di.get(LiteChatFrontController.class).stop();
        }
    }
}
