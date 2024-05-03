package sfri.mhmd.utils.cdi;

public class Context {
    private final String name;
    
    public Context(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Context ctx && ctx.name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
