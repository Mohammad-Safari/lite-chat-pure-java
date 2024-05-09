package sfri.mhmd.utils.cdi;

import java.util.List;

public interface BaseConstructor {
    /**
     * adding object containing {@link sfri.mhmd.utils.cdi.anno.Provider} methods
     * 
     * @param <T>
     * @param dependencyClass
     * @return
     */
    public void addProvider(Object provider);

    /**
     * adding all objects containing {@link sfri.mhmd.utils.cdi.anno.Provider}
     * methods
     * 
     * @param <T>
     * @param dependencyClass
     * @return
     */
    public void addProviders(List<Object> providers);

    /**
     * clear the list of provider objects
     */
    public void removeAllProviders();

    /**
     * create an object by annotated constructor
     * 
     * the way parameters are provided is customizable
     * 
     * note that it does not add the finally returned instance in injector
     * 
     * @param <T>
     * @param dependencyClass
     * @param dependencyProvider gets method/constructor dependency class &
     *                           parameter type, then provides its value/impl
     * @return
     */
    public <T> T construct(Class<T> dependencyClass, ParameterProvider dependencyProvider);
}
