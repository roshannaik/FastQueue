package hwx.storm;

import java.util.concurrent.atomic.AtomicBoolean;

import hwx.Timer;

/**
 * Created by rnaik on 8/10/16.
 */
public class SpoutExecutor extends Thread {
    private final Spout spout;
    private final AtomicBoolean stop = new AtomicBoolean(false);

    public SpoutExecutor(Spout spout) {
        this.spout = spout;
    }


    @Override
    public void run() {
        Timer t1 = new Timer("Spout");
        int count=0;
        while( ! stop.get() ) {
            if( ! spout.nextTuple() ) {
                ++count;
            }  else {
                t1.stop(count);
                return;
            }

        }
    }
}
