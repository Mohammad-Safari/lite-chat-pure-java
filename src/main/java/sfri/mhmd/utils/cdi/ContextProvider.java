package sfri.mhmd.utils.cdi;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

public class ContextProvider {
    @Getter
    @Setter
    private static ContextProvider contextProvider;
    private final Map<Context, BaseInjector> contextDependencies;

    public ContextProvider(Map<Context, BaseInjector> contexDependecies) {
        this.contextDependencies = contexDependecies;
    }

    public void addContext(Context context, BaseInjector di) {
        if (contextDependencies.containsKey(context)) {
            throw new IllegalStateException("this context has been added and still exists!");
        }
        contextDependencies.put(context, di);
    }

    public void removeContext(Context context) {
        contextDependencies.remove(context);

    }

    public void clearAllContexts() {
        contextDependencies.clear();
    }

    public BaseInjector getContextDependencyInjector(Context context) {
        if (!contextDependencies.containsKey(context)) {
            throw new IllegalStateException("the context does not exists!");
        }
        return contextDependencies.get(context);
    }
}
