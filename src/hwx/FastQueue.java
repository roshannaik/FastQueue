package hwx;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by rnaik on 7/28/16.
 */
class FastQueue {
    private int[] elements;
    private AtomicLong headAndTail = new AtomicLong(0);
    public static final long tailMask = 0x000000007fffffffL;  // to drop higher 33 bits

    public static final long headIncrementVal = 0x0000000100000000L;
    public static final long tailIncrementVal = 0x0000000000000001L;


    public FastQueue(int capacity) {
        if(capacity<=0) {
            throw new IllegalArgumentException("Array capacity must be +ve");
        }
        this.elements = new int[capacity+1];   // 1 extra element to differentiate empty and full cases
    }

    /** returns -1 if empty, else returns position of head */
    public int isEmpty() {
        long ht = headAndTail.get();
        long head = ht >>> 32;
        long tail = ht & tailMask;
        return ( head == tail ) ? -1 : (int) head;
    }

    /** returns -1 if full, else returns position of tail */
    public int isFull() {
        long ht = headAndTail.get();
        long head = ht >>> 32;
        long tail = ht & tailMask;
        if (tail + 1 == head) {
            return -1;
        }
        if ( (tail+1)%elements.length == head) {
            return -1;
        }
        return (int)tail;
    }

    private int getHead() {
        return  (int) (headAndTail.get() >>> 32);
    }

    private int getTail() {
        return (int) ( headAndTail.get() & tailMask );
    }

    /** returns false if Q is full */
    public boolean insert(int val) {
        System.err.print("inserting " + val + " ");
        int tail = isFull();
        if (tail == -1) {
            return false;
        }

        elements[tail] = val;
        incrementTail(tail);
        return true;
    }

    /** returns -1 if Q is empty */
    public int remove() {
        System.err.print("removing ");
        int head = isEmpty();
        if (head == -1) {
            return -1;
        }

        int result = elements[head];
        incrementHead(head);
        return result;
    }

    // returns new value of tail
    private void incrementTail(int tail) {
        if (tail==elements.length-1) {
            resetTail(tail);
            return;
        }
        headAndTail.getAndAdd(tailIncrementVal);
    }

    private void incrementHead(int head) {
        if (head==elements.length-1) {
            resetHead(head);
            return;
        }
        headAndTail.getAndAdd(headIncrementVal) ;
    }

    private void resetHead(long head) {
        head <<= 32;
        headAndTail.getAndAdd(-head) ;
    }

    private void resetTail(long tail) {
        headAndTail.getAndAdd(-tail) ;
    }

    public void print() {
        System.err.print("{ ");
        int head = getHead();
        int tail = getTail();
        if(head<tail) {
            for (int i = head; i < tail; ++i)
                System.err.print(elements[i] + " ");
        }
        else if(head>tail) {
            for (int i = head; i < elements.length; ++i)
                System.err.print(elements[i] + " ");
            for (int i = 0; i < tail; ++i)
                System.err.print(elements[i] + " ");
        }
        System.err.println( "} - head " + getHead() + ", tail " + getTail() );
    }

    public static void test() {
        FastQueue fq = new FastQueue(2);
        System.err.println(fq.remove());   // fail { }
        fq.print();
        System.err.println(fq.insert(1)); // {1}
        fq.print();
        System.err.println(fq.insert(2)); // {1 2}
        fq.print();
        System.err.println(fq.insert(3)); // fail {1 2}
        fq.print();

        System.err.println(fq.remove());  // {2}
        fq.print();
        System.err.println(fq.remove());  // {}
        fq.print();
        System.err.println(fq.remove());  // fail {}
        fq.print();
        System.err.println(fq.insert(4)); // {4}
        fq.print();
        System.err.println(fq.insert(5)); // {4 5}
        fq.print();

        System.err.println(fq.remove());  // {5}
        fq.print();

        System.err.println(fq.insert(6)); // {5 6}
        fq.print();
        System.err.println(fq.insert(7)); // fail {7}
        fq.print();
    }
}
