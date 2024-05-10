package sfri.mhmd.utils.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sfri.mhmd.utils.cdi.BaseConstructor;
import sfri.mhmd.utils.cdi.BaseInjector;
import sfri.mhmd.utils.cdi.Context;
import sfri.mhmd.utils.cdi.ContextProvider;
import sfri.mhmd.utils.cdi.anno.Provider;

@RequiredArgsConstructor
public class LiteChatContextAwareController implements LiteChatController {
    @Getter
    private final String path;
    private final ContextAwareRunnable contextAwareRunnable;

    public static Context getContext(HttpExchange x) {
        var contextName = Thread.currentThread().getName() + "-" + x.getHttpContext().hashCode();
        return new Context(contextName);
    }

    @Override
    public void handle(HttpExchange x) throws Throwable {
        var contextProvider = ContextProvider.getContextProvider();
        var rootConstructor = contextProvider.getRootContextConstructor();
        var baseInjector = rootConstructor.construct(BaseInjector.class, null);
        var baseConstructor = rootConstructor.construct(BaseConstructor.class, (clazz, type) -> baseInjector);

        var context = getContext(x);
        contextProvider.addContext(context, baseInjector, baseConstructor);

        baseInjector.set(HttpExchange.class, x);
        baseInjector.set(GsonBuilder.class, new GsonBuilder());
        baseConstructor.addProvider(this);

        contextAwareRunnable.handle(context);

        contextProvider.removeContext(context);
    }

    @Override
    public boolean matches(HttpExchange x) {
        return LiteChatController.super.matches(x);
    }

    @Provider
    public Gson provideGson(GsonBuilder builder) {
        return builder.create();
    }
}
