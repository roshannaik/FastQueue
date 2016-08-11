package hwx;


import java.util.ArrayDeque;


// L2 cache 256 KB
// L3 cache 6 MB
public class Main {

    public static void main(String[] args) {
//        Main.sleep(10_000);
        System.err.println("++++ ---- FastQ ---- ++++");
        final int size = 2_000_000;  // 2 mill
        final int times = size * 40;

        runSequentially_JavaArrayQ();
//        runSequentially_AL();
//        runSequentially_LA();
        runSequentially_LA2();
//        System.err.println("\n");
//        runConcurrently_AL(size, times);

//        run_LA_debugging();
//        runConcurrently_LA(size, times);
//        runConcurrently_LA_MPSC(10, size, times);
//        runConcurrently_LABatched(size, times);


    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void runSequentially_JavaArrayQ() {

        final int size = 20_000_000;

        System.err.println("\nJava[ArrayQueue] ----  Sequential Run: ------ Q size: " + size);

        Integer zero = new Integer(0);
        ArrayDeque<Integer> dq = new ArrayDeque<>(size);
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < size; ++i) {
                dq.add(zero);
            }

            long done = System.currentTimeMillis();
            long perMs = size / (done - start);  // per millisec
            long perSec = perMs / 1000; // million/sec
            System.err.println((done - start) + " ms - producer" +
                    ".   Throughput: " + perMs + " /ms OR " + perSec + " M/sec");
        }
        {
            long start = System.currentTimeMillis();
            for (int i = 0; i < size; ++i) {
                dq.remove();
            }

            long done = System.currentTimeMillis();
            long perMs = size / (done - start);  // per millisec
            long perSec = perMs / 1000; // million/sec
            System.err.println((done - start) + " ms - consumer." +
                    ".   Throughput: " + perMs + " /ms OR " + perSec + " M/sec");
        }


    }
    private static void runSequentially_AL() {

        final int size = 2_000_000;
        FastQueue q = new FastQueue(size);
        final int times = size * 1;

        System.err.println("\n[AtomicLong] ----  Sequential Run: ------ Q size: " + size);
        Producer p = new Producer(q, times);
        Consumer c = new Consumer(q, times);

        try {
            p.start();
            p.join();
            Thread.sleep(1);
            c.start();
            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }


    private static void runSequentially_LA() {

        final int size = 20_000_000;
        FastQueue_LA q = new FastQueue_LA(size);
        final int times = size;

        System.err.println("\n[LongAdder] ----  Sequential Run: ------ Q size: " + size);
        ProducerLA<FastQueue_LA> p = new ProducerLA<>(q, times);
        ConsumerLA<FastQueue_LA> c = new ConsumerLA<>(q, times);

        try {
            p.start();
            p.join();
            Thread.sleep(1);
            c.start();
            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }

    private static void runSequentially_LA2() {

        final int size = 20_000_000;
        FastQueue_LA_2 q = new FastQueue_LA_2(size);
        final int times = size;

        System.err.println("\n[LongAdder] ----  Sequential Run FQ_2 : ------ Q size: " + size);
        ProducerLA<FastQueue_LA_2> p = new ProducerLA<>(q, times);
        ConsumerLA<FastQueue_LA_2> c = new ConsumerLA<>(q, times);

        try {
            p.start();
            p.join();
            Thread.sleep(1);
            c.start();
            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }

    private static void run_LA_debugging() {
        final int size = 4;
        FastQueue_LA q = new FastQueue_LA(size);
        final int times = size * 10;

        System.err.println("\n[LongAdder] ----  Sequential Run: ------ Q size: " + size);
        ProducerLA p = new ProducerLA(q, times);
        ConsumerLA c = new ConsumerLA(q, times);

        try {
            p.start();
            Thread.sleep(1000);
//            p.q.print();
            c.start();
            p.join();
            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }


    private static void runConcurrently_AL(int size, int times) {

        FastQueue q = new FastQueue(size);
        System.err.println("\n[AtomicLong] ----  Concurrent Run: ------ Q size: " + size + ", times: " + times);
        Producer p = new Producer(q, times);
        Consumer c = new Consumer(q, times);

        try {
            p.start();
            Thread.sleep(1);
            c.start();
            p.join();
            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }

    private static void runConcurrently_LA(int size, int times) {
        FastQueue_LA q = new FastQueue_LA(size);

        System.err.println("\n[LongAdder] ----  Concurrent Run: ------ Q size: " + size + ", times: " + times);
        ProducerLA p = new ProducerLA(q, times);
        ConsumerLA c = new ConsumerLA(q, times);

        try {
            p.start();
            Thread.sleep(0,100);
            c.start();
            p.join();
            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }

    private static void runConcurrently_LABatched(int size, int times) {
        FastQueue_LA q = new FastQueue_LA(size);

        System.err.println("\n[LongAdder] ----  Concurrent Run: ------ Q size: " + size + ", times: " + times);
        BatchedProducerLA p = new BatchedProducerLA(q, times, 64);
        ConsumerLA c = new ConsumerLA(q, times);

        try {
            p.start();
            Thread.sleep(1);
            c.start();
            p.join();
            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }

    private static void runConcurrently_LA_MPSC(int producerCount, int size, int times) {
        System.err.println("\n[LongAdder] ----  MultiProducer Concurrent Run: ------ Q size: " + size + ", times: " + times);
        FastQueue_LA[] qs = new FastQueue_LA[producerCount];
        for(int i=0; i<producerCount; ++i)
            qs[i] = new FastQueue_LA(size);

        ProducerLA[] producers = new ProducerLA[producerCount];
        for(int i=0; i<producerCount; ++i)
            producers[i] = new ProducerLA(qs[i], times);

        ConsumerLA_mulitQ c = new ConsumerLA_mulitQ(producerCount, times, qs);

        try {
            c.start();
            for(int i=0; i<producerCount; ++i)
                producers[i].start();

            for(int i=0; i<producerCount; ++i)
                producers[i].join();

            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }

    static class Producer extends Thread {
        private final FastQueue q;
        private final int times;

        public Producer(FastQueue q, int times) {
            this.q = q;
            this.times = times;
        }

        @Override
        public void run() {
            int fails=0;
            long start = System.currentTimeMillis();
            int i=0;
            while(i<times) {
                if(! q.insert(i) )
                    ++fails;
                else
                    ++i;
            }
            long done = System.currentTimeMillis();
            long perMs = times / (done - start);  // per millisec
            long perSec = perMs / 1000; // mill/sec
            System.err.println( (done- start) + " ms - producer - fail count = " + fails +
                                 ".   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
        }
    }


    static class Consumer extends Thread {
        private final FastQueue q;
        private final int max;
        long p1=0,p2=0,p3=0,p4=0,p5=0,p6=0,p7=0,p8=0;

        public Consumer(FastQueue q, int max) {
            this.q = q;
            this.max = max;
        }

        @Override
        public void run() {
            int count=0, fails=0;
            long start = System.currentTimeMillis();
            while(true) {
                int val = q.remove();
                if( val==-1 ) {
                    ++fails;
                    Main.sleep(1);
                } else {
                    ++count;
                    if(count==max) {
                        long done = System.currentTimeMillis();
                        long perMs = max / (done - start);  // per millisec
                        long perSec = perMs / 1000; // million/sec
                        System.err.println( (done- start) + " ms - consumer - fail count " + fails +
                                         ".   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
                        return;
                    }
                }
            } //for
        }
    }

     static class ProducerLA <Q extends FQ> extends Thread {
        private final Q q;
        private final int max;
        long p1=0,p2=0,p3=0,p4=0,p5=0,p6=0,p7=0,p8=0;

        public ProducerLA(Q q, int max) {
            this.q = q;
            this.max = max;
        }

        @Override
        public void run() {
            int fails=0;
            long start = System.currentTimeMillis();
            Integer zero = new Integer(0);
            int i=0;
            while(i<max) {
                if(! q.insert(zero) ) {
                    ++fails;
                    yield();
//                    Main.sleep(1);
                }
                else
                    ++i;
            }
            long done = System.currentTimeMillis();
            long perMs = max / (done - start);  // per millisec
            long perSec = perMs / 1000; // million/sec
            System.err.println( (done- start) + " ms - producer - fail count " + fails +  ", total " + i +
                           ".   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
        }
        private long dummy() {
            return p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8;
        }

    } // class ProducerLA

    static class BatchedProducerLA extends Thread {
        private final FastQueue_LA q;
        private final int max;
        private final int batchSize;
        Integer[] arr;

        public BatchedProducerLA(FastQueue_LA q, int max, int batchSize) {
            this.q = q;
            this.max = max;
            this.batchSize = batchSize;
            arr = new Integer[batchSize];
            for(int i=0; i<batchSize; ++i) {
                arr[i] = i;
            }
        }

        @Override
        public void run() {
            int fails=0;
            long start = System.currentTimeMillis();
            for(int i=0; i<max; i+=batchSize) {
                if( q.insertAll(arr) == 0 )
                    ++fails;
            }
            long done = System.currentTimeMillis();
            long perMs = max / (done - start);  // per milliSec
            long perSec = perMs / 1000; // million/sec
            System.err.println( (done- start) + " ms - producer - fail count " + fails +
                    ".   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
        }
    }


    static  class ConsumerLA <Q extends FQ> extends Thread {
        private final Q q;
        private final int max;
        long p1=0,p2=0,p3=0,p4=0,p5=0,p6=0,p7=0,p8=0;

        public ConsumerLA(Q q, int max) {
            this.q = q;
            this.max = max;
        }

        @Override
        public void run() {
            int count=0, fails=0;
            long start = System.currentTimeMillis();
            while(true) {
                Integer val = q.remove();
//                if( val==-1 ) {
//                    ++fails;
////                    yield();
//                    sleep(1);
//                } else {
                    ++count;
                    if(count==max) {
                        long done = System.currentTimeMillis();
                        long perMs = max / (done - start);  // per millisec
                        long perSec = perMs / 1000; // million/sec
                        System.err.println( (done- start) + " ms - consumer - fail count " + fails +
                                         ".   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");

                        return;
                    }
//                }
            } //for
        } // run()

        private static void sleep(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        private long dummy() {
            return p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8;
        }

    } // class ConsumerLA

    static class ConsumerLA_mulitQ extends Thread {
        private final int producerCount;
        private final int max;
        public FastQueue_LA[] queues = null;
        int[] counts;
        boolean[] dones;
        int fails=0;
        int doneCount=0;



        public ConsumerLA_mulitQ(int producerCount, int times, FastQueue_LA... queues) {
            this.producerCount = producerCount;
            this.max = times;
            this.queues = queues;
            this.dones = new boolean[producerCount];
            this.counts = new int[producerCount];
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            while(doneCount<queues.length) {
                for(int i=0; i<producerCount; ++i)
                    remove(i);
            } //while
            long done = System.currentTimeMillis();
            long perMs = max / (done - start);  // per millisec
            long perSec = perMs / 1000; // million/sec
            System.err.println( (done- start) + " ms - consumer - fail count " + fails +
                    ".   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");

        } // run()
        private void remove(int i) {
            if(dones[i])
                return;
            Integer val = queues[i].remove();
            if( val==-1 ) {
                ++fails;
                    yield();
//                sleep(1);
            } else {
                ++counts[i];
                if(counts[i]==max) {
                    ++doneCount;
                }
            }
        }

        private static void sleep(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}







class FastQueue_LA_2 implements FQ {
    private final int capacity;
    private Integer[] elements;
    transient private int head = 0;
    transient private int tail = 0;

    //    private LongAdder headAndTail = new LongAdder();
    public static final long tailMask = 0x000000007fffffffL;  // to drop higher 33 bits

    public static final long headIncrementVal = 0x0000000100000000L;
    public static final long tailIncrementVal = 0x0000000000000001L;
    //    private long p1=0,p2=0,p3=0,p4=0,p5=0,p6=0;
    private long len = 0;


    public FastQueue_LA_2(int capacity) {
        this.capacity = capacity;

        if(this.capacity<=0) {
            throw new IllegalArgumentException("Array capacity must be +ve");
        }
        int arrSize = getArrSize(capacity+1); // 1 extra element to differentiate empty and full cases
        this.elements = new Integer[arrSize];
        len = elements.length;
    }

    private int getArrSize(int numElements) {
        int result = 8;
        // Find the best power of two to hold elements.
        // Tests "<=" because arrays aren't kept full.
        if (numElements >= result) {
            result = numElements;
            result |= (result >>>  1);
            result |= (result >>>  2);
            result |= (result >>>  4);
            result |= (result >>>  8);
            result |= (result >>> 16);
            result++;

            if (result < 0)   // Too many elements, must back off
                result >>>= 1;// Good luck allocating 2 ^ 30 elements
        }
        return result;
    }

    /** returns -1 if empty, else returns position of head */
    public int isEmpty() {
        return ( head == tail ) ? -1 : head;
    }

    /** returns -1 if full, else returns position of tail */
    public int isFull() {
        if (tail + 1 == head) {
            return -1;
        }
        if ( (tail+1)%len == head) {
            return -1;
        }
        return tail;
    }

    private int getHead() {
        return  head;
    }

    private int getTail() {
        return tail;
    }

    /** returns false if Q is full */
    public boolean insert(Integer val) {
        int tail = isFull();
        if (tail != -1) {
            elements[tail] = val;
            incrementTail();
            return true;

        }
        return false;
    }

    private int size() {
        if(tail>head)
            return tail - head;
        else if (tail<head)
            return (elements.length - head) + tail  -1; // account for 1 extra slot
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
        incrementHead();
        return result;
    }

    // returns new value of tail
    private void incrementTail() {
        if (tail==elements.length-1) {
            resetTail();
            return;
        }
        ++tail;
    }

    private void incrementHead() {
        if (head==elements.length-1) {
            resetHead();
            return;
        }
        ++head;
    }

    private void resetHead() {
        head = 0;
    }

    private void resetTail() {
        tail =0 ;
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