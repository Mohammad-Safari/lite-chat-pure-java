package sfri.mhmd.utils.cdi;

import java.lang.reflect.Constructor;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import sfri.mhmd.utils.cdi.anno.Inject;
import sfri.mhmd.utils.cdi.anno.Optional;

@SuppressWarnings("unchecked")
public class DependencyConstructor implements BaseConstructor {
    private final BaseInjector dependencyInjector;

    public DependencyConstructor(DependencyInjector dependencyInjector) {
        this.dependencyInjector = dependencyInjector;
    }

    public <T> T constructDefective(Class<T> dependencyClass) {
        ParameterProvider dependencyProvider = ParameterProviders.nullParameterProvider(dependencyInjector, this);
        return construct(dependencyClass, dependencyProvider);
    }

    public <T> T constructShallow(Class<T> dependencyClass) {
        ParameterProvider dependencyProvider = ParameterProviders.shallowParameterProvider(dependencyInjector, this);
        return construct(dependencyClass, dependencyProvider);
    }

    public <T> T constructDeep(Class<T> dependencyClass) {
        ParameterProvider dependencyProvider = ParameterProviders.deepParameterProvider(dependencyInjector, this);
        return construct(dependencyClass, dependencyProvider);
    }

    /**
     * create an object by annotated constructor
     * 
     * the way parameters are provided is customizable
     * 
     * note that it does not add the finally returned instance in injector
     * 
     * @param <T>
     * @param dependencyClass
     * @param dependencyProvider gets param/field class and dependency class and
     *                           provides a value for it
     * @return
     */
    public <T> T construct(Class<T> dependencyClass, ParameterProvider dependencyProvider) {
        Constructor<T>[] constructors = (Constructor<T>[]) dependencyClass.getDeclaredConstructors();
        for (Constructor<T> constructor : constructors) {
            if (!constructor.isAnnotationPresent(Inject.class)) {
                continue;
            }
            Parameter[] requiredParams = constructor.getParameters();
            List<Object> requiredParamValues = new ArrayList<>();
            for (Parameter requiredParam : requiredParams) {
                try {
                    requiredParamValues.add(
                            dependencyProvider.apply(requiredParam.getType(), requiredParam.getParameterizedType()));
                } catch (Exception e) {
                    if (!requiredParam.isAnnotationPresent(Optional.class)) {
                        throw new RuntimeException("Error injecting dependency: " + requiredParam.getName());
                    }
                    requiredParamValues.add(null);
                }
            }
            try {
                if (requiredParamValues.size() == requiredParams.length) {
                    return constructor.newInstance(requiredParamValues.toArray());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error constructing dependnecy: " + e.getMessage());
            }
        }
        throw new RuntimeException("Error constructing with insufficient params");
    }

}
