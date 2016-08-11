package hwx;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Created by rnaik on 7/29/16.
 */
public class AtomicsSpeed {
    static volatile int i=0;
    public static void main(String[] args) throws InterruptedException {
        System.err.println("---- ATOMICS SPEED TEST ---");
        int times = 100_000_000;
        runSequentially_Basic(times);

        runSequentially_Volatile(times);
        runConcurrently_Volatile(times, 1);
        runConcurrently_Volatile(times, 2);
        runConcurrently_Volatile(times, 3);
        runConcurrently_Volatile(times, 4);

        runConcurrently_LazySet(times, 1);
        runConcurrently_LazySet(times, 2);
        runConcurrently_LazySet(times, 3);
        runConcurrently_LazySet(times, 4);

        runSequentially_LA(times);
        runSequentially_AL(times);

        runConcurrent_LA(times);
        runConcurrent_AL(times);

    }

    private static void runSequentially_Basic(int times) throws InterruptedException {
        System.err.println("\n[int] Sequential Run: ------ Iterations : " + times);
        long start = System.currentTimeMillis();
        int counter=0;
        for(int i =0; i<times; ++i)
            ++counter;
        for(int i =0; i<times; ++i)
            --counter;

        long done = System.currentTimeMillis();
        long perMs = times / (done - start);  // per milliSec
        long perSec = perMs / 1000; // million/sec
        System.err.println((done - start) + " ms - producer done");
        System.err.println("   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");

    }

    private static void runSequentially_Volatile(int times) throws InterruptedException {
        System.err.println("\n[volatile int] Sequential Run: ------ Iterations : " + times);
        Timer t = new Timer("Writer");

        int counter=0;
        int x = 0;
        for(; i<times; ++i)
            ++counter;
        t.stop(times);

        t = new Timer("Reader");
        for(i =0; i<times; ++i)
            x = counter;
        t.stop(times);
//        long done = System.currentTimeMillis();
//        long perMs = times / (done - start);  // per milliSec
//        long perSec = perMs / 1000; // million/sec
//        System.err.println((done - start) + " ms - producer done");
//        System.err.println("   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");

    }

    private static void startAll(Thread[] producers) {
        for(Thread p  : producers)
            p.start();
    }

    private static void joinAll(Thread[] producers) throws InterruptedException {
        for(Thread p  : producers)
            p.join();
    }

    private static void runConcurrently_Volatile(int times, int producerCount) throws InterruptedException {
        System.err.println("\n[volatile int] Concurrent Run. Producers : " + producerCount + ": ------ Iterations : " + times);
        SharedVolatile sobj = new SharedVolatile();

        ConsumerVolatile c = new ConsumerVolatile(sobj, times);

        ProducerVolatile[] producers = new ProducerVolatile[producerCount];
        for(int i=0; i<producerCount; ++i)
            producers[i] = new ProducerVolatile(sobj, times/producerCount);
        startAll(producers);
        c.start();
        joinAll(producers);
        sobj.vi  = times;
        c.join();
    }


    private static void runConcurrently_LazySet(int times, int producerCount) throws InterruptedException {
        System.err.println("\n[lazy set int] Concurrent Run. Producers : " + producerCount + ": ------ Iterations : " + times);
        AtomicInteger sobj = new AtomicInteger(0);

        ConsumerLazySet c = new ConsumerLazySet(sobj, times);

        ProducerLazySet[] producers = new ProducerLazySet[producerCount];
        for(int i=0; i<producerCount; ++i)
            producers[i] = new ProducerLazySet(sobj, times/producerCount);
        startAll(producers);
        c.start();
        joinAll(producers);
        sobj.lazySet(times);
        c.join();
    }


    private static void runSequentially_LA(int times) throws InterruptedException {
        System.err.println("\n[LongAdder] Sequential Run: ------ Iterations : " + times);
        LongAdder la = new LongAdder();
        ProducerLA p = new ProducerLA(la, times);
        ConsumerLA c = new ConsumerLA(la, times);
        p.start();
        p.join();
        c.start();
        c.join();
    }

    private static void runConcurrent_LA(int times) throws InterruptedException {
        System.err.println("\n[LongAdder] Concurrent Run: ------ Iterations : " + times);
        LongAdder la = new LongAdder();
        ProducerLA p = new ProducerLA(la, times);
        ConsumerLA c = new ConsumerLA(la, times);
        p.start();
        c.start();
        p.join();
        c.join();
    }


    private static void runSequentially_AL(int times) throws InterruptedException {
        System.err.println("\n[AtomicLong] Sequential Run: ------ Iterations : " + times);
        AtomicLong al = new AtomicLong(0);
        ProducerAL p = new ProducerAL(al, times);
        ConsumerAL c = new ConsumerAL(al, times);
        p.start();
        p.join();
        c.start();
        c.join();
    }

    private static void runConcurrent_AL(int times) throws InterruptedException {
        System.err.println("\n[AtomicLong] Concurrent Run: ------ Iterations : " + times);
        AtomicLong al = new AtomicLong(0);
        ProducerAL p = new ProducerAL(al, times);
        ConsumerAL c = new ConsumerAL(al, times);
        p.start();
        c.start();
        p.join();
        c.join();
    }



    //-------- LazySet

    static class ProducerLazySet extends Thread {
        private final AtomicInteger obj;
        private final int max;

        public ProducerLazySet(AtomicInteger o, int max) {
            this.obj = o;
            this.max = max;
        }

        @Override
        public void run() {
            Timer t = new Timer("Writer");
            for (int i = 0; i < max; ++i) {
                obj.lazySet(i);
            }
            t.stop(max);
        }
    } // ProducerLazySet


    static class ConsumerLazySet extends Thread {
        private final AtomicInteger obj;
        private final int max;
        public int last =0;

        public ConsumerLazySet(AtomicInteger o, int max) {
            this.obj = o;
            this.max = max;
        }

        @Override
        public void run() {
            Timer t = new Timer("Reader");
            long i = 0;
            for (; last < max-1; ++i) {
                last = obj.get();
            }
            t.stop(max);
            System.err.println("Attempts " + i + ", Failed attempts = " +( (i>max) ? (i - max) : 0) );
        }
    } // ConsumerLazySet


    //------- Volatile

    static class SharedVolatile {
        public volatile int vi = 0;
    }

    static class ProducerVolatile extends Thread {
        private final SharedVolatile obj;
        private final int max;

        public ProducerVolatile(SharedVolatile o, int max) {
            this.obj = o;
            this.max = max;
        }

        @Override
        public void run() {
            Timer t = new Timer("Writer");
            for (int i = 0; i < max; ++i) {
                obj.vi = i;
            }
            t.stop(max);
        }
    } // ProducerVolatile


    static class ConsumerVolatile extends Thread {
        private final SharedVolatile obj;
        private final int max;
        public int last =0;

        public ConsumerVolatile(SharedVolatile o, int max) {
            this.obj = o;
            this.max = max;
        }

        @Override
        public void run() {
            Timer t = new Timer("Reader");
            long i = 0;
            for (; last < max-1; ++i) {
                last = obj.vi;
            }
            t.stop(max);
            System.err.println("Attempts " + i + ", Failed attempts = " +( (i>max) ? (i - max) : 0) );
        }
    } // ConsumerVolatile


    // ---------   LONG ADDER
    static class ConsumerLA extends Thread {
        private final LongAdder la;
        private final int max;

        public ConsumerLA(LongAdder la, int max) {
            this.la = la;
            this.max = max;
        }

        @Override
        public void run() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < max; ++i) {
            la.add(-1);
        }
        long done = System.currentTimeMillis();
        long perMs = max / (done - start);  // per milliSec
        long perSec = perMs / 1000; // million/sec
        System.err.println((done - start) + " ms - producer done");
        System.err.println("   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
        }
    } // ConsumerLA


    static class ProducerLA extends Thread {
        private final LongAdder la;
        private final int max;

        public ProducerLA(LongAdder la, int max) {
            this.la = la;
            this.max = max;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            for (int i = 0; i < max; ++i) {
                la.add(1);
            }
            long done = System.currentTimeMillis();
            long perMs = max / (done - start);  // per milliSec
            long perSec = perMs / 1000; // million/sec
            System.err.println((done - start) + " ms - producer done");
            System.err.println("   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
        }
    } // ProducerLA

    static class ConsumerAL extends Thread {
        private final AtomicLong al;
        private final int max;

        public ConsumerAL(AtomicLong al, int max) {
            this.al = al;
            this.max = max;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            for (int i = 0; i < max; ++i) {
                al.getAndAdd(-11);
            }
            long done = System.currentTimeMillis();
            long perMs = max / (done - start);  // per milliSec
            long perSec = perMs / 1000; // million/sec
            System.err.println((done - start) + " ms - producer done");
            System.err.println("   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
        }
    } // ConsumerAL


    static class ProducerAL extends Thread {
        private final AtomicLong al;
        private final int max;

        public ProducerAL(AtomicLong la, int max) {
            this.al = la;
            this.max = max;
        }

        @Override
        public void run() {
            long start = System.currentTimeMillis();
            for (int i = 0; i < max; ++i) {
                al.getAndAdd(1);
            }
            long done = System.currentTimeMillis();
            long perMs = max / (done - start);  // per milliSec
            long perSec = perMs / 1000; // million/sec
            System.err.println((done - start) + " ms - producer done");
            System.err.println("   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
        }
    } // ProducerAL


}
