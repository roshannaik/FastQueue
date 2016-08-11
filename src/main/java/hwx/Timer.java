package hwx;

/**
 * Created by rnaik on 8/9/16.
 */


public class Timer {
    private final String who;
    private long start, done=0;
    public Timer(String who) {
        this.who = who;
        start = System.currentTimeMillis();
    }

    public void stop(int tupleCount) {
        done = System.currentTimeMillis();
        long perMs = tupleCount / (done - start);  // per millisec
        long perSec = perMs / 1000; // million/sec
        System.err.println((done - start) + " ms - " + who +
                " Throughput: " + perMs + " /ms OR " + perSec + " M/sec");
    }
}
