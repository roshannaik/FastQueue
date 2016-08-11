package hwx;


import java.util.concurrent.atomic.AtomicInteger;



class Head {
    private long p1,p2,p3,p4,p5,p6,p7,p8; // cacheline padding from other objs
    AtomicInteger head = new AtomicInteger(0);
}

class HeadAndTail extends Head {
    private long p1,p2,p3,p4,p5,p6,p7,p8; // cacheline padding from head
    AtomicInteger tail = new AtomicInteger(0);
}

public class FastQueue2<T> extends  HeadAndTail implements Qu<T> {
    private long p1,p2,p3,p4,p5,p6,p7,p8; // cacheline padding from head and tail
    private Object[] elements;

    public FastQueue2(int capacity) {
        if (capacity < 1)  {
            throw new IllegalArgumentException("Array capacity must be +ve");
        }
        if (Integer.bitCount(capacity) != 1)  {
            throw new IllegalArgumentException("bufferSize must be a power of 2");
        }

        this.elements = new Object[capacity+1];   // 1 extra element to differentiate empty and full cases
    }

    /** returns -1 if empty, else returns position of head */
    public int isEmpty() {
        int h = head.get();
        int t = tail.get();
        return ( h == t ) ? -1 : h;
    }

    /** returns -1 if full, else returns position of tail */
    public int isFull() {
        int h = head.get();
        int t = tail.get();
        if (t + 1 == h) {
            return -1;
        }
        if ( (t+1)%elements.length == h) {
            return -1;
        }
        return t;
    }

    private int getHead() {
        return  head.get();
    }

    private int getTail() {
        return tail.get();
    }

    /** returns false if Q is full */
    public boolean insert(T val) {
        int t = isFull();
        if (t == -1) {
            return false;
        }

        elements[t] = val;
        incrementTail(t);
        return true;
    }

    /** returns null if Q is empty */
    public T remove() {
        int h = isEmpty();
        if (h == -1) {
            return null;
        }

        Object result = elements[h];
        incrementHead(h);
        return (T) result;
    }

    private void incrementTail(int t) {
        if (t==elements.length-1) {
            resetTail();
            return;
        }
        tail.lazySet(t+1);
    }

    private void incrementHead(int h) {
        if (h==elements.length-1) {
            resetHead();
            return;
        }
        head.lazySet(h+1);
    }

    private void resetHead() {
        head.lazySet(0);
    }

    private void resetTail() {
        tail.lazySet(0);
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


    public static void main(String[] args) {
        final int size = 2 * 1024 * 1024;
        final int times = size * 40;

        run1Producer1Consumer(size, times);

        runConcurrently_LA_MPSC(1, size, times);
//        runConcurrently_LA_MPSC(2, size, times);
//        runConcurrently_LA_MPSC(3, size, times);
//        runConcurrently_LA_MPSC(4, size, times);
//        runConcurrently_LA_MPSC(8, size, times);
    }

    private static void runConcurrently_LA_MPSC(int producerCount, int size, int times) {
        System.err.println("\n[FastQueue2] ----  Producers: " + producerCount + " Concurrent Run: ------ Q size: " + size + ", times: " + times);

        FastQueue2[] qs = new FastQueue2[producerCount];
        for(int i=0; i<producerCount; ++i)
            qs[i] = new FastQueue2(size);

        Producer[] producers = new Producer[producerCount];
        for(int i=0; i<producerCount; ++i)
            producers[i] = new Producer(qs[i], times);

        Consumer_mulitQ c = new Consumer_mulitQ(producerCount, times, qs);

        try {
            for(int i=0; i<producerCount; ++i)
                producers[i].start();

            c.start();

            for(int i=0; i<producerCount; ++i)
                producers[i].join();

            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }

    private static void run1Producer1Consumer(int size, int times) {
        FastQueue2 q = new FastQueue2(size);
        System.err.println("\n[FastQueue2] ----  1 Producer 1 Consumer: ------ Q size: " + size);
        Producer p = new Producer(q, times);
        Consumer c = new Consumer(q, times);

        try {
            p.start();
            c.start();
            p.join();
            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }


    static class Producer extends Thread {
        private final Qu q;
        private final int max;
        long p1=0,p2=0,p3=0,p4=0,p5=0,p6=0,p7=0,p8=0;

        public Producer(Qu q, int max) {
            this.q = q;
            this.max = max;
        }

        @Override
        public void run() {
            int fails=0;
            long start = System.currentTimeMillis();

            int i=0;
            while(i<max) {
                if(! q.insert(i) ) {
                    ++fails;
                    yield();
//                    Main.sleep(1);
                }
                else
                    ++i;
            }
            long done = System.currentTimeMillis();
            long perMs = max / (done - start);  // per milliSec
            long perSec = perMs / 1000; // million/sec
            System.err.println( (done- start) + " ms - producer - fail count " + fails +  ", total " + i +
                    ".   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
        }
        private long dummy() {
            return p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8;
        }

    } // class Producer


    static  class Consumer  extends Thread {
        private final Qu<Integer> q;
        private final int max;
        long p1=0,p2=0,p3=0,p4=0,p5=0,p6=0,p7=0,p8=0;

        public Consumer(Qu q, int max) {
            this.q = q;
            this.max = max;
        }

        @Override
        public void run() {
            int count=0, fails=0;
            long start = System.currentTimeMillis();
            while(true) {
                Integer val = q.remove();
                if( val==null ) {
                    ++fails;
//                    yield();
                    sleep(1);
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
            } // while
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


    static class Consumer_mulitQ extends Thread {
        private final int producerCount;
        private final int max;
        public FastQueue2<Integer>[] queues = null;
        int[] counts;
        boolean[] dones;
        int fails=0;
        int doneCount=0;



        public Consumer_mulitQ(int producerCount, int times, FastQueue2... queues) {
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
                for(int i=0; i<producerCount; ++i) {
                    for (int j = 0; j < 30; j++) {
                        remove(i);
                    }
                }

            } //while
            long done = System.currentTimeMillis();
            long perMs = max / (done - start);  // per millisec
            long perSec = perMs / 1000; // million/sec
            System.err.println( (done- start) + " ms - consumer - fail count " + fails +
                    ".   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");

        } // run()
        private int remove(int i) {
            if(dones[i])
                return -1;
            Integer val = queues[i].remove();
            if( val==null ) {
                ++fails;
//                yield();
//                sleep(1);
                return -1;
            } else {
                ++counts[i];
                if(counts[i]==max) {
                    ++doneCount;
                }
                return val;
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



} // class FastQueue2
