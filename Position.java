

/**
 * Generic position abstraction.
 * @param <T> Element type.
 */
public interface Position<T> {
    /**
     * Read element.
     * @return Element at this position.
     */
    T get();

    /**
     * Write element.
     * @param t Element to store at this position.
     */
    void put(T t);
}
