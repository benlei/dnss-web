package dnss.tools.commons;

public interface Items<E, R> {
    public void add(E item);
    public R poll();
}
