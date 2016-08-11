package hwx;


public class Main2 {

    public static void main(String[] args) {
        System.err.println("FastQ - LongAdder\n");
        runSequentially_LA();
        runConcurrently_LA();

    }

    private static void runSequentially_LA() {
        final int size = 10000000 * 20;
        FastQueue_LA q = new FastQueue_LA(size);
        final int times = size * 1;

        System.err.println("Sequential Run: ------ Q size: " + size);
        ProducerLA p = new ProducerLA(q, times);
        ConsumerLA c = new ConsumerLA(q, times);

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

    private static void runConcurrently_LA() {
        final int size = 2000000;
        FastQueue_LA q = new FastQueue_LA(size);
        final int times = size * 20;

        System.err.println("Concurrent Run: ------ Q size: " + size + ", times: " + times);
        ProducerLA p = new ProducerLA(q, times);
        ConsumerLA c = new ConsumerLA(q, times);

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

    static class ProducerLA extends Thread {
        private final FastQueue_LA q;
        private final int max;

        public ProducerLA(FastQueue_LA q, int max) {
            this.q = q;
            this.max = max;
        }

        @Override
        public void run() {
            int fails=0;
            long start = System.currentTimeMillis();
            for(int i=0; i<max; ++i) {
                if(! q.insert(i) )
                    ++fails;
            }
            long done = System.currentTimeMillis();
            System.err.println( (done- start) + " ms - producer done - fail count = " + fails);
        }
    }


    static  class ConsumerLA extends Thread {
        private final FastQueue_LA q;
        private final int max;

        public ConsumerLA(FastQueue_LA q, int max) {
            this.q = q;
            this.max = max;
        }

        @Override
        public void run() {
            int count=0, fails=0;
            long start = System.currentTimeMillis();
            while(true) {
                Integer val = q.remove();
                if( val==-1 ) {
                    ++fails;
                    sleep(1);
                } else {
                    ++count;
                    if(count==max) {
                        long done = System.currentTimeMillis();
                        System.err.println( (done- start) + " ms - consumer done - fail count " + fails);
                        return;
                    }
                }
            } //for
        }

        private static void sleep(int ms) {
            try {
                Thread.sleep(ms);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
} //Main2

