package sfri.mhmd.utils.cdi;

import java.util.List;

public interface BaseInjector {
    /**
     * get the registered impl for the type
     * 
     * @param <T>
     * @param dependencyType
     * @return
     */
    public <T> T get(Class<T> dependencyKey);

    /**
     * get all the registered implementations for the type
     * 
     * @param <T>
     * @param dependencyType
     * @return
     */
    public <T> List<T> getAll(Class<T> dependencyKey);

    /**
     * checks if the dependency could have a list of implementations
     * (based on initially used registration flag)
     * 
     * @param <T>
     * @param dependencyKey
     * @return
     */
    public <T> boolean isMulti(Class<T> dependencyKey);

    /**
     * registers a type with its implementation(s) in DI map
     * 
     * @param <T>
     * @param dependencyType
     * @param implementation
     * @param multi
     */
    public <T> void set(Class<T> dependencyKey, T implementation);

    /**
     * register a type with one impl
     * 
     * @param <T>
     * @param dependencyType
     * @param implementation
     */
    public <T> void set(Class<T> dependencyKey, T implementation, boolean multi);

    /**
     * register a list of implementations
     * 
     * @param <T>
     * @param dependencyType
     * @param implementations
     */
    public <T> void setAll(Class<T> dependencyType, List<T> implementations);

    /**
     * inject dependencies inside the preconstructed/defective object
     * 
     * @param object
     */
    public <T> void inject(T object);

}
