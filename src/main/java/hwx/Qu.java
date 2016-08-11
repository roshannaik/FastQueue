package hwx;

public interface Qu<T> {
    int isFull();  /** returns -1 if full, else returns position of tail */
    int isEmpty(); /** returns -1 if full, else returns position of head */

    /** returns false if Q is full */
    boolean insert(T val) ;

    T remove();
}
