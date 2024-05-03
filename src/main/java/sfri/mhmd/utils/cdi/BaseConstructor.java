package sfri.mhmd.utils.cdi;

public interface BaseConstructor {
    public <T> T construct(Class<T> dependencyClass, ParameterProvider dependencyProvider);
}
