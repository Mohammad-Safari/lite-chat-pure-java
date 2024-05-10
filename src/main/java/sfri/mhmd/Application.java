package sfri.mhmd;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import sfri.mhmd.utils.cdi.Context;
import sfri.mhmd.utils.cdi.ContextProvider;
import sfri.mhmd.utils.cdi.anno.Provider;
import sfri.mhmd.utils.cdi.dependency.DependencyConstructor;
import sfri.mhmd.utils.cdi.dependency.DependencyInjector;
import sfri.mhmd.utils.cdi.dependency.ParameterProviders;
import sfri.mhmd.utils.server.LiteChatContextAwareController;
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
                },
                new LiteChatResourceResolver(".*(\\.).*"),
                new LiteChatContextAwareController("/chats",
                        (context) -> {
                            var contextProvider = ContextProvider.getContextProvider();
                            var injector = contextProvider.getContextInjector(context);
                            var cosntructor = contextProvider.getContextConstrucor(context);
                            var gson = cosntructor.construct(Gson.class,
                                    ParameterProviders.shallowParameterProvider(injector, cosntructor));
                            var x = injector.get(HttpExchange.class);
                            var response = gson.toJson(List.of("chat1", "chat2")).getBytes();
                            x.sendResponseHeaders(200, response.length);
                            x.getResponseBody().write(response);
                        })));

        startApp(di, dc);
    }

    private static void initContext() {
        di = provideInjector();
        dc = provideCosntructor(di);
        dc.addProvider(new Application());
        ContextProvider.setContextProvider(provideContext());
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

    private static ContextProvider provideContext() {
        return new ContextProvider(new ConcurrentHashMap<>(), new ConcurrentHashMap<>());
    }

    @Provider(clazz = "sfri.mhmd.utils.cdi.BaseInjector")
    public static DependencyInjector provideInjector() {
        return new DependencyInjector(new ConcurrentHashMap<>());
    }

    @Provider(clazz = "sfri.mhmd.utils.cdi.BaseConstructor")
    public static DependencyConstructor provideCosntructor(DependencyInjector di) {
        return new DependencyConstructor(di, new CopyOnWriteArrayList<>());
    }
}
