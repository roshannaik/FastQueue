package hwx;

/**
 * Created by rnaik on 8/4/16.
 */
public interface FQ {
    /** returns -1 if full, else returns position of tail */
    int isFull();

    /** returns false if Q is full */
    boolean insert(Integer val) ;

    Integer remove();
}
