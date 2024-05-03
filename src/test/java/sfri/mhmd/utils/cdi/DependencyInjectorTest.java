package sfri.mhmd.utils.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sfri.mhmd.utils.cdi.anno.Inject;

public class DependencyInjectorTest {

    private class DummyClass {
    }

    private class AnotherDummyClass extends DummyClass {
    }

    private DependencyInjector dependencyInjector;

    @BeforeEach
    void createInstance() {
        dependencyInjector = new DependencyInjector(new HashMap<>());
    }

    @Test
    void testSetAndGet() {
        var impl = new DummyClass();
        dependencyInjector.set(DummyClass.class, impl);
        assertEquals(dependencyInjector.get(DummyClass.class), impl);
    }

    @Test
    void testSetAndGetAll() {
        var impl = new DummyClass();
        var anotherImpl = new AnotherDummyClass();
        dependencyInjector.set(DummyClass.class, impl, true);
        dependencyInjector.set(DummyClass.class, anotherImpl, true);
        assertTrue(dependencyInjector.getAll(DummyClass.class).contains(impl));
        assertTrue(dependencyInjector.getAll(DummyClass.class).contains(anotherImpl));
    }

    @Test
    void testInject() {
        class DummyOwner {
            @Inject
            private DummyClass dummy;
        }
        var owner = new DummyOwner();
        var impl = new AnotherDummyClass();
        dependencyInjector.set(DummyClass.class, impl, false);
        dependencyInjector.inject(owner);
        assertEquals(owner.dummy, impl);

    }

    @Test
    void testInjectMulti() {
        class DummyOwner {
            @Inject
            private List<DummyClass> dummies;
        }
        var owner = new DummyOwner();
        var impl = new DummyClass();
        var anotherImpl = new AnotherDummyClass();
        dependencyInjector.set(DummyClass.class, impl, true);
        dependencyInjector.set(DummyClass.class, anotherImpl, true);
        dependencyInjector.inject(owner);
        assertEquals(owner.dummies.size(), 2);
        assertTrue(owner.dummies.contains(impl));
        assertTrue(owner.dummies.contains(anotherImpl));

    }

    @Test
    void testIsMulti() {
        var impl = new DummyClass();
        var anotherImpl = new AnotherDummyClass();
        dependencyInjector.set(DummyClass.class, impl, true);
        dependencyInjector.set(DummyClass.class, anotherImpl, true);
        assertTrue(dependencyInjector.isMulti(DummyClass.class));
    }
}
