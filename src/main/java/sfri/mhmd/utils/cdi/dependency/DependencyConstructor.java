package sfri.mhmd.utils.cdi.dependency;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import sfri.mhmd.utils.cdi.BaseConstructor;
import sfri.mhmd.utils.cdi.BaseInjector;
import sfri.mhmd.utils.cdi.ParameterProvider;
import sfri.mhmd.utils.cdi.anno.Inject;
import sfri.mhmd.utils.cdi.anno.Optional;
import sfri.mhmd.utils.cdi.anno.Provider;

@RequiredArgsConstructor
@SuppressWarnings("unchecked")
public class DependencyConstructor implements BaseConstructor {
    private final List<Object> providerObjects;

    @Override
    public void addProvider(Object provider) {
        providerObjects.add(provider);
    }

    @Override
    public void addProviders(List<Object> providers) {
        providerObjects.addAll(providers);
    }

    @Override
    public void removeAllProviders() {
        providerObjects.clear();
    }

    @Override
    public <T> T construct(Class<T> dependencyClass, ParameterProvider dependencyProvider) {
        T instance = providerObjects.stream()
                .map(obj -> fromProviderMethods(obj, dependencyClass, dependencyProvider))
                .filter(Objects::nonNull)
                .findFirst().orElse(null);
        return instance == null ? fromAnnotatedConstructors(dependencyClass, dependencyProvider) : instance;
    }

    /**
     * construct an instance based on
     * {@link ParameterProviders#nullParameterProvider} policy
     * 
     * @param <T>
     * @param dependencyClass
     * @return
     */
    public static <T> T constructDefective(BaseConstructor bc, Class<T> dependencyClass) {
        ParameterProvider dependencyProvider = ParameterProviders.nullParameterProvider(null, bc);
        return bc.construct(dependencyClass, dependencyProvider);
    }

    /**
     * construct an instance based on
     * {@link ParameterProviders#shallowParameterProvider} policy
     * 
     * @param <T>
     * @param dependencyClass
     * @return
     */
    public static <T> T constructShallow(BaseInjector bi, BaseConstructor bc, Class<T> dependencyClass) {
        ParameterProvider dependencyProvider = ParameterProviders.shallowParameterProvider(bi, bc);
        return bc.construct(dependencyClass, dependencyProvider);
    }

    /**
     * construct an instance based on
     * {@link ParameterProviders#deepParameterProvider} policy
     * 
     * @param <T>
     * @param dependencyClass
     * @return
     */
    public static <T> T constructDeep(BaseInjector bi, BaseConstructor bc, Class<T> dependencyClass) {
        ParameterProvider dependencyProvider = ParameterProviders.deepParameterProvider(bi, bc);
        return bc.construct(dependencyClass, dependencyProvider);
    }

    /**
     * create the instance from first possible-matching provider method with
     * {@link sfri.mhmd.utils.cdi.anno.Provider} mark from the registered
     * provider objects
     * 
     * @param <T>
     * @param providerObject
     * @param dependencyClass
     * @param dependencyProvider
     * @return
     */
    private <T> T fromProviderMethods(Object providerObject, Class<T> dependencyClass,
            ParameterProvider dependencyProvider) {
        Method[] providers = providerObject.getClass().getDeclaredMethods();
        for (Method provider : providers) {
            if (!provider.isAnnotationPresent(Provider.class)) {
                continue;
            }
            var explicitClasses = Arrays.stream(provider.getAnnotationsByType(Provider.class))
                    .map(Provider::clazz).filter(s -> !s.equals(""))
                    .map(this::ClassForNameOrNull).filter(Objects::nonNull).toList();
            if (provider.getReturnType() != dependencyClass && !explicitClasses.contains(dependencyClass)) {
                continue;
            }
            Parameter[] requiredParams = provider.getParameters();
            List<Object> requiredParamValues = Arrays.stream(requiredParams)
                    .map(requiredParam -> provideParamter(requiredParam, dependencyProvider)).toList();
            try {
                if (requiredParamValues.size() == requiredParams.length) {
                    return (T) provider.invoke(providerObject, requiredParamValues.toArray());
                }
            } catch (Exception e) {
                throw new RuntimeException(
                        "Error constructing dependency: " + provider.getName() + ": " + e.getMessage());
            }
        }
        return null;
    }

    private Class<?> ClassForNameOrNull(String clazz) {
        try {
            return Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * create the instance from first possible-matching provider method
     * 
     * @param <T>
     * @param providerObject
     * @param dependencyClass
     * @param dependencyProvider
     * @return
     */
    private <T> T fromAnnotatedConstructors(Class<T> dependencyClass, ParameterProvider dependencyProvider) {
        Constructor<T>[] constructors = (Constructor<T>[]) dependencyClass.getDeclaredConstructors();
        for (Constructor<T> constructor : constructors) {
            if (!constructor.isAnnotationPresent(Inject.class)) {
                continue;
            }
            Parameter[] requiredParams = constructor.getParameters();
            List<Object> requiredParamValues = Arrays.stream(requiredParams)
                    .map(requiredParam -> provideParamter(requiredParam, dependencyProvider)).toList();
            try {
                if (requiredParamValues.size() == requiredParams.length) {
                    return constructor.newInstance(requiredParamValues.toArray());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error constructing dependnecy: " + e.getMessage());
            }
        }
        throw new RuntimeException("No matching annotated constructor is available for instantiation");
    }

    private Object provideParamter(Parameter requiredParam, ParameterProvider dependencyProvider) {
        try {
            return dependencyProvider.apply(requiredParam.getType(), requiredParam.getParameterizedType());
        } catch (Exception e) {
            if (!requiredParam.isAnnotationPresent(Optional.class)) {
                throw new RuntimeException(
                        "Error injecting dependency: " + requiredParam.getName() + ": " + e.getMessage());
            }
            return null;
        }
    }

}
