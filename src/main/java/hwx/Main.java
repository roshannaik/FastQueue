package hwx;


public class Main {

    public static void main(String[] args) {
        runSequentially();
        runConcurrently();

    }

    private static void runSequentially() {
        System.err.println("Sequential Run: ------");
        final int size = 10000000 * 20;
        FastQueue q = new FastQueue(size);
        final int times = size * 1;

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
        System.err.println("Concurrent Run: ------");
        final int size = 10000000 * 20;
        FastQueue q = new FastQueue(size);
        final int times = size * 1;

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

}

class Producer extends Thread {
    private final FastQueue q;
    private final int max;

    public Producer(FastQueue q, int max) {
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
        System.err.println( (done- start) + " - producer done - fail count = " + fails);
    }
}


class Consumer extends Thread {
    private final FastQueue q;
    private final int max;

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
                sleep(1);
            } else {
                ++count;
                if(count==max) {
                    long done = System.currentTimeMillis();
                    System.err.println( (done- start) + " - consumer done - fail count " + fails);
                    return;
                }
            }
        } //for
    }

    private void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}