package sfri.mhmd.utils.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import sfri.mhmd.utils.cdi.anno.Inject;
import sfri.mhmd.utils.cdi.anno.Provider;
import sfri.mhmd.utils.cdi.dependency.DependencyConstructor;
import sfri.mhmd.utils.cdi.dependency.DependencyInjector;

@ExtendWith(MockitoExtension.class)
public class DependencyConstructorTest {

    /*
     * note inner non-static classes have extra hidden param in their constructors
     */

    protected static class DummyDependencyClass {
    }

    protected static class DummyClass {
        private DummyDependencyClass dep;

        public DummyClass() {
        }

        @Inject
        public DummyClass(DummyDependencyClass dep) {
            this.dep = dep;
        }

        @Provider
        public DummyDependencyClass provider() {
            return new DummyDependencyClass();
        }
    }

    protected static class AnotherDummyClass extends DummyClass {
    }

    protected static class DummyOwner {
        private List<DummyClass> dummies;

        @Inject
        public DummyOwner(List<DummyClass> dummies) {
            this.dummies = dummies;
        }
    }

    private DependencyConstructor dependencyConstructor;
    private DependencyInjector dependencyInjector;

    @BeforeEach
    void createInstance() {
        dependencyInjector = new DependencyInjector(new HashMap<>());
        dependencyConstructor = new DependencyConstructor(new ArrayList<>());
    }

    @Test
    void testConstruct() {
        var dummyClass = new DummyClass();
        ParameterProvider dummyPovider = (x, y) -> List.of(dummyClass);
        var owner = dependencyConstructor.construct(DummyOwner.class, dummyPovider);
        assertTrue(owner.dummies.contains(dummyClass));
        assertEquals(owner.dummies.size(), 1);
    }

    @Test
    void testConstructDeep() {
        var dep = new DummyDependencyClass();
        dependencyInjector.set(DummyDependencyClass.class, dep, false);
        var owner = DependencyConstructor.constructDeep(dependencyInjector, dependencyConstructor, DummyOwner.class);
        assertNotNull(owner.dummies);
        assertNotNull(owner.dummies.getFirst());
        assertNotNull(owner.dummies.getFirst().dep);
        assertEquals(owner.dummies.size(), 1);
    }

    @Test
    void testConstructDefective() {
        var owner = DependencyConstructor.constructDefective(dependencyConstructor, DummyOwner.class);
        assertNull(owner.dummies);
    }

    @Test
    void testConstructShallow() {
        var impl = new DummyClass();
        var anotherImpl = new AnotherDummyClass();
        dependencyInjector.set(DummyClass.class, impl, true);
        dependencyInjector.set(DummyClass.class, anotherImpl, true);
        var owner = DependencyConstructor.constructShallow(dependencyInjector, dependencyConstructor, DummyOwner.class);
        assertNotNull(owner.dummies);
        assertNotNull(owner.dummies.getFirst());
        assertNull(owner.dummies.getFirst().dep);
        assertEquals(owner.dummies.size(), 2);
    }

    @Test
    void testConstructWithProvider() {
        var providerObjectForDummyDep = spy(new DummyClass());
        dependencyConstructor.addProvider(providerObjectForDummyDep);
        var dummyObj = DependencyConstructor.constructShallow(dependencyInjector, dependencyConstructor,
                DummyDependencyClass.class);
        assertNotNull(dummyObj);
        verify(providerObjectForDummyDep, times(1)).provider();

    }

    @Test
    void testConstructMixedProviderAndConstructor() {
        var providerObjectForDummyDep = spy(new DummyClass()); // just as a provider object
        dependencyConstructor.addProvider(providerObjectForDummyDep);
        var owner = DependencyConstructor.constructDeep(dependencyInjector, dependencyConstructor, DummyOwner.class);
        assertNotNull(owner.dummies);
        assertNotNull(owner.dummies.getFirst());
        assertNotNull(owner.dummies.getFirst().dep);
        assertEquals(owner.dummies.size(), 1);
        verify(providerObjectForDummyDep, times(1)).provider();

    }
}
