package sfri.mhmd.utils.cdi;

import java.lang.reflect.Type;
import java.util.function.BiFunction;

/**
 * Gets method/constructor dependency classes, parameter type,
 * then provides its value/implmentation
 */
public interface ParameterProvider extends BiFunction<Class<?>, Type, Object> {

}