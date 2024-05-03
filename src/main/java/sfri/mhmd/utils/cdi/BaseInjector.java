package sfri.mhmd.utils.cdi;

import java.util.List;

public interface BaseInjector {

    public <T> T get(Class<T> dependencyKey);

    public <T> List<T> getAll(Class<T> dependencyKey);

    public <T> boolean isMulti(Class<T> dependencyKey);

    public <T> void set(Class<T> dependencyKey, T implementation);

    public <T> void set(Class<T> dependencyKey, T implementation, boolean multi);

    public <T> void inject(T object);

}
