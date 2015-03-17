package dnss.tools.commons;

public interface Accumulator<E, F> {
    public void accumulate(E element);
    public F dissipate();
}
