package hwx;


import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by rnaik on 7/28/16.
 */
class FastQueue_LA implements FQ {
    private Integer[] elements;
    private MyHeadTail headAndTail = new MyHeadTail();
//    private LongAdder headAndTail = new LongAdder();
    public static final long tailMask = 0x000000007fffffffL;  // to drop higher 33 bits

    public static final long headIncrementVal = 0x0000000100000000L;
    public static final long tailIncrementVal = 0x0000000000000001L;
//    private long p1=0,p2=0,p3=0,p4=0,p5=0,p6=0;
    private long len = 0;


    public FastQueue_LA(int capacity) {

        if(capacity<=0) {
            throw new IllegalArgumentException("Array capacity must be +ve");
        }
        this.elements = new Integer[capacity+1];   // 1 extra element to differentiate empty and full cases
        len = elements.length;
    }

    /** returns -1 if empty, else returns position of head */
    public int isEmpty() {
        long ht = headAndTail.longValue();
        long head = ht >>> 32;
        long tail = ht & tailMask;
        return ( head == tail ) ? -1 : (int) head;
    }

    /** returns -1 if full, else returns position of tail */
    public int isFull() {
        long ht = headAndTail.longValue();
        long head = ht >>> 32;
        long tail = ht & tailMask;
        if (tail + 1 == head) {
            return -1;
        }
        if ( (tail+1)%len == head) {
            return -1;
        }
        return (int)tail;
    }

    private int getHead() {
        return  (int) (headAndTail.longValue() >>> 32);
    }

    private int getTail() {
        return (int) ( headAndTail.longValue() & tailMask );
    }

    /** returns false if Q is full */
    public boolean insert(Integer val) {
        int tail = isFull();
        if (tail != -1) {
            elements[tail] = val;
            incrementTail(tail);
            return true;

        }
        return false;
    }

//    public boolean insert(Integer val) {
//        int tail = isFull();
//        if (tail == -1) {
//            return false;
//        }
//
//        elements[tail] = val;
//        incrementTail(tail);
//        return true;
//    }

    // returns a count of how many were added to Q
    //todo: squish the 2 fenced loads into 1
    // todo: test
    public int insertAll(Integer[] vals) {
        if(vals.length > elements.length)
            return 0;
        int free = freeSlots();  // Fenced load
        if(free ==0)
            return 0;

        int tail = getTail(); // Fenced load
        int count = 0;
        while(count<=free) {
            elements[tail] = vals[count];
            ++tail;
            ++count;
            if(tail == elements.length) {
                tail=0;
            }
        }
        incrementTail(tail, count);
        return count;
    }

    private int size() {
        long ht = headAndTail.longValue();
        int h = (int) (ht >>> 32);
        int t = (int) ( ht & tailMask );
        if(t>h)
            return t - h;
        else if (t<h)
            return (elements.length - h) + t  -1; // account for 1 extra slot
        else
            return 0;
    }

    private int freeSlots() {
        return (elements.length-1) - size();
    }

    /** returns -1 if Q is empty */
    public Integer remove() {
        int head = isEmpty();
        if (head == -1) {
            return -1;
        }

        Integer result = elements[head];
        incrementHead(head);
        return result;
    }

    // returns new value of tail
    private void incrementTail(int tail) {
        if (tail==elements.length-1) {
            resetTail(tail);
            return;
        }
        headAndTail.add(tailIncrementVal);
    }

    // returns new value of tail
    private void incrementTail(int tail, int by) {
        int newTail = tail + by;
        if(newTail >= elements.length)
            newTail = newTail - elements.length;

        if(newTail>tail)
            headAndTail.add(newTail-tail);
        else
            headAndTail.add(-(tail-newTail));
    }

    private void incrementHead(int head) {
        if (head==elements.length-1) {
            resetHead(head);
            return;
        }
        headAndTail.add(headIncrementVal) ;
    }

    private void resetHead(long head) {
        head <<= 32;
        headAndTail.add(-head) ;
    }

    private void resetTail(long tail) {
        headAndTail.add(-tail) ;
    }

    public long print() {
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
//        return p1 + p2 + p3 + p4 + p5 + p6;
        return 0;
    }

    class MyHeadTail {
        long ht;

        public MyHeadTail() {
            this.ht = 0;
        }

        public long longValue() {
            return ht;
        }
        public void add(long x) {
            ht+=x;
        }
    }
}
