package sfri.mhmd.utils.cdi;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import sfri.mhmd.utils.cdi.anno.Inject;
import sfri.mhmd.utils.cdi.anno.Optional;

@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class DependencyInjector implements BaseInjector {
    private final Map<Class<?>, Object> dependencies;

    @Override
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

    @Override
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

    @Override
    public <T> boolean isMulti(Class<T> dependencyType) {
        Object dependency = dependencies.get(dependencyType);
        return dependency instanceof List ? true : false;
    }

    @Override
    public <T> void set(Class<T> dependencyType, T implementation, boolean multi) {
        if (!dependencies.containsKey(dependencyType)) {
            dependencies.put(dependencyType, multi ? new ArrayList<T>(List.of(implementation)) : implementation);
            return;
        }
        if (multi && dependencies.get(dependencyType) instanceof List) {
            ((List<T>) dependencies.get(dependencyType)).add(implementation);
        } else {
            throw new IllegalStateException("Dependency already registered: " + dependencyType);
        }
    }

    @Override
    public <T> void set(Class<T> dependencyType, T implementation) {
        set(dependencyType, implementation, false);
    }

    @Override
    public <T> void setAll(Class<T> dependencyType, List<T> implementations) {
        implementations.forEach(impl -> set(dependencyType, impl, true));
    }

    @Override
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