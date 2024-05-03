package sfri.mhmd.utils.cdi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sfri.mhmd.utils.cdi.anno.Inject;
import sfri.mhmd.utils.cdi.anno.Optional;

public class DependencyInjector implements BaseInjector {
    private final Map<Class<?>, Object> dependencies;

    public DependencyInjector(Map<Class<?>, Object> dependencies) {
        this.dependencies = dependencies;
    }

    /**
     * get the registered impl for the type
     * 
     * @param <T>
     * @param dependencyType
     * @return
     */
    public <T> T get(Class<T> dependencyType) {
        T dependency = (T) dependencies.get(dependencyType);
        if (dependency == null) {
            throw new IllegalStateException("Dependency not found: " + dependencyType);
        }
        if (isMulti(dependencyType)) {
            throw new IllegalStateException("Dependency has mutiple Implementations: " + dependencyType);
        }
        return dependency;
    }

    /**
     * get all the registered implementations for the type
     * 
     * @param <T>
     * @param dependencyType
     * @return
     */
    public <T> List<T> getAll(Class<T> dependencyType) {
        List<T> dependency = (List<T>) dependencies.get(dependencyType);
        if (dependency == null) {
            throw new IllegalStateException("Dependency not found: " + dependencyType);
        }
        if (!isMulti(dependencyType)) {
            throw new IllegalStateException("Dependency has only one Implementation: " + dependencyType);
        }
        return (List<T>) dependency;
    }

    public <T> boolean isMulti(Class<T> dependencyType) {
        Object dependency = dependencies.get(dependencyType);
        return dependency instanceof List ? true : false;
    }

    /**
     * registers a type with its implementation(s) in DI map
     * 
     * @param <T>
     * @param dependencyType
     * @param implementation
     * @param multi
     */
    public <T> void set(Class<T> dependencyType, T implementation, boolean multi) {
        if (!dependencies.containsKey(dependencyType)) {
            dependencies.put(dependencyType, multi ? new ArrayList(List.of(implementation)) : implementation);
            return;
        }
        if (multi && dependencies.get(dependencyType) instanceof List) {
            ((List<T>) dependencies.get(dependencyType)).add(implementation);
        } else {
            throw new IllegalStateException("Dependency already registered: " + dependencyType);
        }
    }

    /**
     * register atype with one impl
     * 
     * @param <T>
     * @param dependencyType
     * @param implementation
     */
    public <T> void set(Class<T> dependencyType, T implementation) {
        set(dependencyType, implementation, false);
    }

    /**
     * inject dependencies inside the preconstructed/defective object
     * 
     * @param object
     */
    public <T> void inject(T object) {
        Class<?> clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                field.setAccessible(true); // Allow access to private fields
                try {
                    ParameterProvider provider = ParameterProviders.shallowParameterProvider(this, null);
                    field.set(object, provider.apply(field.getType(), field.getGenericType()));
                } catch (IllegalAccessException | IllegalArgumentException e) {
                    if (!field.isAnnotationPresent(Optional.class)) {
                        throw new RuntimeException("Error injecting dependency: " + e.getMessage());
                    }
                }
            }
        }
    }
}