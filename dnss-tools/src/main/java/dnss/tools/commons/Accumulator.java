package dnss.tools.commons;

public interface Accumulator<E> {
    public void accumulate(E element);
    public void dissipate();
    public int size();
    public int accumulations();
}
