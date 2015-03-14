package dnss.tools.commons;

public interface Accumulator<E> {
    public void accumulate(E obj);

    public void dissipate();
}
