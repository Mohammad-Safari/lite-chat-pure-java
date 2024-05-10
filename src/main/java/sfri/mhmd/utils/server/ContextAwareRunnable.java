package sfri.mhmd.utils.server;

import sfri.mhmd.utils.cdi.Context;

@FunctionalInterface
public interface ContextAwareRunnable {
    void handle(Context context) throws Throwable;
}