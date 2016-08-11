package hwx;

import java.util.concurrent.ArrayBlockingQueue;

public class MainBQ {

    public static void main(String[] args) {
        System.err.println("Java ArrayBlockingQueue\n");
        runSequentially();
        runConcurrently();

    }

    private static void runSequentially() {
        final int size = 1000000 * 20;
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue(size);
        final int times = size * 1;

        System.err.println("\nSequential Run: ------ Q size: " + size);
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

    private static void runConcurrently() {

        final int size = 2000000;
        ArrayBlockingQueue<Integer> q = new ArrayBlockingQueue<>(size);
        final int times = size * 20;

        System.err.println("\nConcurrent Run: ------ Q size: " + size + ", times: " + times);
        Producer p = new Producer(q, times);
        Consumer c = new Consumer(q, times);

        try {
            p.start();
            Thread.sleep(5);
            c.start();
            p.join();
            c.join();
        } catch (InterruptedException e) {
            System.err.println("interrupted");
            e.printStackTrace();
        }
    }

    static class Producer extends Thread {
        private final ArrayBlockingQueue<Integer> q;
        private final int max;

        public Producer(ArrayBlockingQueue<Integer> q, int max) {
            this.q = q;
            this.max = max;
        }

        @Override
        public void run() {
            int fails=0;
            try {
                long start = System.currentTimeMillis();
                for (int i = 0; i < max; ++i) {
                    q.put(i);
                    ++fails;
                }
                long done = System.currentTimeMillis();
                long perMs = max / (done - start);  // per millisec
                long perSec = perMs / 1000; // million/sec
                System.err.println((done - start) + " ms - producer done - fail count = " + fails +
                               "   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
            }catch (InterruptedException e) {
                System.err.println("producer interrupted. exiting");
                return;
            }
        }
    }


    static class Consumer extends Thread {
        private final ArrayBlockingQueue<Integer> q;
        private final int max;

        public Consumer(ArrayBlockingQueue<Integer> q, int max) {
            this.q = q;
            this.max = max;
        }

        @Override
        public void run() {
            int count=0, fails=0;
            try {
                long start = System.currentTimeMillis();
                while (true) {
                    int val = q.take();
                    if (val == -1) {
                        ++fails;
                        sleep(1);
                    } else {
                        ++count;
                        if (count == max) {
                            long done = System.currentTimeMillis();
                            long perMs = max / (done - start);  // per millisec
                            long perSec = perMs / 1000; // million/sec
                            System.err.println((done - start) + " ms - consumer done - fail count " + fails +
                                     "   Throughput: " + perMs + " /ms OR " + perSec +  " M/sec");
                            return;
                        }
                    }
                } //while
            }catch (InterruptedException e) {
                System.err.println("consumer interrupted. exiting");
                return;
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
} // MainBQ
