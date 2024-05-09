package sfri.mhmd.utils.cdi;

import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor
public class ContextProvider {
    @Getter
    @Setter
    private static ContextProvider contextProvider;
    private final Map<Context, BaseInjector> contextInjectors;
    private final Map<Context, BaseConstructor> contextConstructors;
    @Getter
    @Setter
    private BaseInjector rootContextInjector;
    @Getter
    @Setter
    private BaseConstructor rootContextConstructor;

    public void addContext(Context context, BaseInjector di, BaseConstructor dc) {
        if (contextInjectors.containsKey(context) || contextConstructors.containsKey(context)) {
            throw new IllegalStateException("this context has been added and still exists!");
        }
        contextInjectors.put(context, di);
    }

    public void removeContext(Context context) {
        contextInjectors.remove(context);
        contextConstructors.remove(context);
    }

    public void clearAllContexts() {
        contextInjectors.clear();
        contextConstructors.clear();
    }

    public BaseInjector getContextInjector(Context context) {
        if (!contextInjectors.containsKey(context)) {
            throw new IllegalStateException("the context has no injectors!");
        }
        return contextInjectors.get(context);
    }

    public BaseConstructor getContextConstrucors(Context context) {
        if (!contextConstructors.containsKey(context)) {
            throw new IllegalStateException("the context has no constructors!");
        }
        return contextConstructors.get(context);
    }
}
