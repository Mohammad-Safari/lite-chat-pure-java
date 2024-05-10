package sfri.mhmd.utils.cdi.dependency;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import sfri.mhmd.utils.cdi.BaseConstructor;
import sfri.mhmd.utils.cdi.BaseInjector;
import sfri.mhmd.utils.cdi.ParameterProvider;

@SuppressWarnings("unchecked")
class ParameterProviders {
    /**
     * field Type is strictly the class field type which could be actually an array,
     * collection or the same as dependency Type
     * 
     * @param fieldClass
     * @param dependencyClass
     * @return
     */
    static protected <T> Object provideDependencyForField(BaseInjector dependencyInjector, Class<?> fieldClass,
            Class<T> dependencyClass) {
        // @formatter:off
        if (!dependencyInjector.isMulti(dependencyClass)) return dependencyInjector.get(dependencyClass);
        if(fieldClass.isArray()) return dependencyInjector.getAll(dependencyClass).toArray();
        if(fieldClass.getClass().isInstance(List.class)) return dependencyInjector.getAll(dependencyClass);
        if(fieldClass.getClass().isInstance(Set.class)) return Set.copyOf(dependencyInjector.getAll(dependencyClass));
        return null;
        // @formatter:on
    }

    /**
     * recognizes field dependency type the way if it is
     * - the component type of array
     * - the generic type value of collection
     * - or simply the field type
     * 
     * @param field
     * @return
     */
    static protected <T> Class<T> determineDependencyClassForField(Class<?> clazz, Type genricType) {
        if (clazz.isArray()) {
            return (Class<T>) clazz.getComponentType();
        }
        if (genricType instanceof ParameterizedType pt) {
            return pt.getRawType().getClass().isInstance(Collection.class)
                    ? (Class<T>) pt.getActualTypeArguments()[0]
                    : (Class<T>) pt.getRawType();
        }
        return (Class<T>) clazz;
    }

    public static <T> ParameterProvider deepParameterProvider(BaseInjector dependencyInjector,
            BaseConstructor dependencyConstructor) {
        return (Class<?> paramClass, Type paramGenericType) -> {
            Class<T> dependencyClass = determineDependencyClassForField(paramClass, paramGenericType);
            try {
                if (dependencyInjector.isMulti(dependencyClass)) {
                    dependencyInjector.getAll(dependencyClass);
                } else {
                    dependencyInjector.get(dependencyClass);
                }
            } catch (Exception e) {
                var deepParameterProvider = deepParameterProvider(dependencyInjector, dependencyConstructor);
                T constructDeep = dependencyConstructor.construct(dependencyClass, deepParameterProvider);
                dependencyInjector.set(dependencyClass, constructDeep, !dependencyClass.equals(paramClass));
            }
            return provideDependencyForField(dependencyInjector, paramClass, dependencyClass);
        };
    }

    public static <T> ParameterProvider shallowParameterProvider(BaseInjector dependencyInjector,
            BaseConstructor dependencyConstructor) {
        return (Class<?> paramClass, Type paramGenericType) -> {
            Class<T> dependencyClass = determineDependencyClassForField(paramClass, paramGenericType);
            return provideDependencyForField(dependencyInjector, paramClass, dependencyClass);
        };
    }

    public static <T> ParameterProvider nullParameterProvider(BaseInjector dependencyInjector,
            BaseConstructor dependencyConstructor) {
        return (Class<?> paramClass, Type paramGenericType) -> null;
    }

}